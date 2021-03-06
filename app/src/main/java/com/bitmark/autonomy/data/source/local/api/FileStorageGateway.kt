/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.local.api

import android.content.Context
import java.io.File

class FileStorageGateway internal constructor(private val context: Context) {

    fun save(path: String, name: String, data: ByteArray): File {
        val dir = File(path)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, name)
        if (!file.exists()) file.createNewFile()
        file.writeBytes(data)
        return file
    }

    fun saveOnFilesDir(name: String, data: ByteArray) =
        save(context.filesDir.absolutePath, name, data)

    fun isExistingOnFilesDir(name: String): Boolean =
        File(context.filesDir, name).exists()

    fun isExisting(path: String): Boolean = File(path).exists()

    fun read(path: String) = File(path).readBytes()

    fun readOnFilesDir(name: String) = read(File(context.filesDir, name).absolutePath)

    fun filesDir() = context.filesDir

    fun firstFile(path: String): File? {
        val files = listFiles(path)
        return if (files.isNotEmpty()) files[0] else null
    }

    fun listFiles(path: String): List<File> {
        val file = File(path)
        if (!file.exists()) return listOf()
        if (file.isFile) return listOf(file)
        return file.listFiles()?.toList() ?: listOf()
    }

    fun listFiles(dirPath: String, prefix: String): List<File> {
        val dir = File(dirPath)
        if (!dir.isDirectory) error("dirPath is not a directory path")
        return dir.listFiles { f -> f.name.startsWith(prefix) }?.toList()
            ?: error("unexpected occurred")
    }

    fun delete(path: String): Boolean {
        val file = File(path)
        if (!file.exists()) return true
        return if (file.isDirectory) file.deleteRecursively() else file.delete()
    }

    fun delete(dirPath: String, prefix: String): Boolean {
        val files = listFiles(dirPath, prefix)
        if (files.isEmpty()) return true
        var result = true
        files.forEach { f -> result = result && delete(f.absolutePath) }
        return result
    }
}