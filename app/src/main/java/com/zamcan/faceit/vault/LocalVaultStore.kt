package com.zamcan.faceit.vault

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Private, offline vault backed by an Android Keystore AES-256-GCM key.
 * File names are random IDs; names and contents are encrypted together.
 * No cloud account, network access, or broad storage permission is required.
 */
class LocalVaultStore(context: Context) {
    private val appContext = context.applicationContext
    private val directory = File(appContext.noBackupFilesDir, "faceit-vault").apply { mkdirs() }

    fun store(displayName: String, clearBytes: ByteArray): String {
        require(displayName.isNotBlank())
        require(clearBytes.isNotEmpty())
        val id = UUID.randomUUID().toString()
        val payload = ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { out ->
                out.writeUTF(displayName.take(512))
                out.writeInt(clearBytes.size)
                out.write(clearBytes)
            }
            bytes.toByteArray()
        }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(payload)
        val target = File(directory, "$id.vlt")
        val temporary = File(directory, "$id.tmp")
        try {
            DataOutputStream(temporary.outputStream()).use { out ->
                out.writeInt(FORMAT_VERSION)
                out.writeInt(cipher.iv.size)
                out.write(cipher.iv)
                out.writeInt(encrypted.size)
                out.write(encrypted)
                out.flush()
            }
            if (!temporary.renameTo(target)) error("Could not commit encrypted vault item")
        } finally {
            if (temporary.exists()) temporary.delete()
        }
        return id
    }

    fun open(id: String): VaultItem {
        require(ID_PATTERN.matches(id)) { "Invalid vault item identifier" }
        val file = File(directory, "$id.vlt")
        require(file.isFile) { "Vault item not found" }
        val clear = DataInputStream(file.inputStream()).use { input ->
            require(input.readInt() == FORMAT_VERSION) { "Unsupported vault item format" }
            val ivSize = input.readInt()
            require(ivSize == GCM_IV_BYTES)
            val iv = ByteArray(ivSize).also(input::readFully)
            val encryptedSize = input.readInt()
            require(encryptedSize in 17..MAX_ENCRYPTED_BYTES && encryptedSize == input.available())
            val encrypted = ByteArray(encryptedSize).also(input::readFully)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            cipher.doFinal(encrypted)
        }
        return DataInputStream(ByteArrayInputStream(clear)).use { input ->
            val name = input.readUTF()
            val size = input.readInt()
            require(size in 1..MAX_ITEM_BYTES && size == input.available())
            VaultItem(id, name, ByteArray(size).also(input::readFully))
        }
    }

    fun delete(id: String): Boolean {
        require(ID_PATTERN.matches(id)) { "Invalid vault item identifier" }
        return File(directory, "$id.vlt").let { !it.exists() || it.delete() }
    }

    private fun getOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    data class VaultItem(val id: String, val displayName: String, val bytes: ByteArray)

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "faceit.local.vault.aes256"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val FORMAT_VERSION = 1
        const val GCM_IV_BYTES = 12
        const val GCM_TAG_BITS = 128
        const val MAX_ITEM_BYTES = 25 * 1024 * 1024
        const val MAX_ENCRYPTED_BYTES = MAX_ITEM_BYTES + 1024
        val ID_PATTERN = Regex("[0-9a-fA-F-]{36}")
    }
}
