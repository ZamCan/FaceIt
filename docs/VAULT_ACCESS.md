# Vault access boundary

Vault files retain the existing Android Keystore AES-256-GCM format and key alias.
No keys are replaced or ciphertext rewritten by the access-gate update.

Opening a file requires Android's device-credential confirmation (PIN, password
or pattern). A cancelled prompt, missing pending request, unavailable prompt or
device without a secure lock never proceeds to decryption through the UI.
Export keeps only the encrypted record ID while the document picker is open,
then authenticates before decrypting and writing the user-selected clear copy.
There is no general unlocked session.

The visible decrypted buffer is cleared on dismissal, screen disposal or
backgrounding. Import and export run off the main thread; temporary payload and
export buffers are cleared after use where the app owns them. Managed-memory
clearing is best effort, not a forensic erasure guarantee.

This is an app-level access gate. Existing Keystore keys are not silently
migrated to authentication-bound keys. Biometric-specific cryptographic gating,
key-invalidation recovery, encrypted cloud backup and device migration remain
separate work and must not be advertised as implemented. Key-loss behaviour
continues to preserve encrypted records without generating a replacement key.

Release checks must exercise credential success/cancellation, screen-lock
changes, backgrounding, missing/corrupt keys and files, picker cancellation,
export failures and process recreation on physical devices. Compilation and
navigation smoke checks alone do not certify Vault security.

