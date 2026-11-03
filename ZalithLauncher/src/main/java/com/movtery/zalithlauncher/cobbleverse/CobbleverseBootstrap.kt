package com.movtery.zalithlauncher.cobbleverse

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toUri
import org.json.JSONObject
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.downloadFile
import com.movtery.zalithlauncher.utils.network.fetchStringFromUrl
import com.movtery.zalithlauncher.viewmodel.ModpackImportOperation
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

/**
 * COBBLEVERSE: na abertura, lê o cobbleverse-pack.json e, se a versão do pack mudou,
 * baixa o zip do pack e importa com o importador de modpacks que o Zalith já tem.
 * (Software modificado, baseado no Zalith Launcher 2 - GPL-3.0.)
 */
object CobbleverseBootstrap {
    private const val TAG = "CobbleverseBootstrap"

    /** >>> TROQUE pela URL real do seu cobbleverse-pack.json <<< */
    const val PACK_JSON_URL = "https://raw.githubusercontent.com/xlubx/ZalithLauncher2/main/cobbleverse-pack.json"

    private const val PREFS = "cobbleverse_bootstrap"
    private const val KEY_INSTALLED = "installed_pack_version"

    /** Formato do cobbleverse-pack.json (lido com org.json, sem reflexão) */
    private class PackInfo(json: JSONObject) {
        val version: String? = json.optString("version").ifBlank { null }
        val versionName: String? = json.optString("version_name").ifBlank { null } // nome da versão no launcher
        val packZipUrl: String? = json.optString("pack_zip_url").ifBlank { null }
        val packZipSha1: String? = json.optString("pack_zip_sha1").ifBlank { null }  // opcional
    }

    suspend fun runIfNeeded(
        context: Context,
        vm: ModpackImportViewModel,
        keepScreen: suspend (Boolean) -> Unit
    ) {
        if (PACK_JSON_URL.contains("SEU-USUARIO")) return // URL ainda não configurada

        val info = try {
            PackInfo(JSONObject(fetchStringFromUrl(PACK_JSON_URL)))
        } catch (e: Exception) {
            Logger.warning(TAG, "Não foi possível ler o pack.json (offline?)", e)
            return
        }
        val version = info.version ?: return
        val zipUrl = info.packZipUrl ?: return

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getString(KEY_INSTALLED, null) == version) return

        toast(context, "COBBLEVERSE: baixando o pack $version…")
        val zip = File(context.cacheDir, "cobbleverse/pack.zip")
        try {
            withContext(Dispatchers.IO) {
                zip.parentFile?.mkdirs()
                zip.delete()
                downloadFile(url = zipUrl, outputFile = zip, sha1 = info.packZipSha1)
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "Falha ao baixar o pack", e)
            toast(context, "COBBLEVERSE: falha ao baixar o pack. Tente de novo mais tarde.")
            return
        }

        val uri: Uri = zip.toUri()
        withContext(Dispatchers.Main) {
            vm.import(
                context = context,
                uri = uri,
                fixedVersionName = info.versionName ?: "COBBLEVERSE",
                onStart = { /* a tela fica acesa via keepScreen abaixo */ },
                onStop = { }
            )
        }
        keepScreen(true)
        // espera sair do estado "Import": Finished (ok), Error/NotSupport (falha) ou None (cancelado)
        val result = snapshotFlow { vm.importOperation }.first { it !is ModpackImportOperation.Import }
        keepScreen(false)
        if (result is ModpackImportOperation.Finished) {
            prefs.edit().putString(KEY_INSTALLED, version).apply()
        }
    }

    private suspend fun toast(context: Context, msg: String) = withContext(Dispatchers.Main) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}
