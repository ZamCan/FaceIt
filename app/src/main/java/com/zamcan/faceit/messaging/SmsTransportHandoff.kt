package com.zamcan.faceit.messaging

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens the user's chosen SMS application with a plain-text draft.
 * FaceIt does not send SMS in the background and never rewrites transport text
 * to simulate fonts, colours, or rich-text formatting unsupported by SMS.
 */
object SmsTransportHandoff {
    fun openComposer(context: Context, recipient: String, body: String) {
        require(recipient.isNotBlank()) { "A recipient is required" }
        val intent = Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts("smsto", recipient, null)
        ).putExtra("sms_body", body)
        context.startActivity(intent)
    }

    fun plainTextForTransport(styledEditorText: CharSequence): String =
        styledEditorText.toString()
}
