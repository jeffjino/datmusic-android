/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.extensions

import android.content.Context
import android.net.Uri

fun Context.writeToFile(data: ByteArray, output: Uri) {
    // try to reset file before writing
    // non-local files aren't allowed to be opened with write-truncate (wt) mode so do it safely before writing data
    runCatching {
        contentResolver.openOutputStream(output, "wt")?.apply {
            write(byteArrayOf())
            close()
        }
    }
    runCatching {
        val outputStream = contentResolver.openOutputStream(output) ?: error("Failed to open output file stream")
        outputStream.write(data)
        outputStream.close()
    }.onFailure {
        error("Failed to write to file: $output")
    }
}

fun Context.readFromFile(input: Uri): String {
    val inputStream = contentResolver.openInputStream(input) ?: error("Failed to open input file stream")
    return inputStream.bufferedReader().readText()
}
