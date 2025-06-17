package com.guri.guriresumerewrite

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import javax.inject.Singleton

@Singleton
class RemoteConfigImpl(
    private val gson: Gson
) : RemoteConfig {

    private fun <T> read(param: ConfigParam, returnType: Class<T>): T? {
        val value: Any? = when (returnType) {
            String::class.java -> config.getString(param.key)
            Boolean::class.java -> config.getBoolean(param.key)
            Long::class.java -> config.getLong(param.key)
            Int::class.java -> config.getLong(param.key).toInt()
            Double::class.java -> config.getDouble(param.key)
            Float::class.java -> config.getDouble(param.key).toFloat()
            else -> {
                val json = config.getString(param.key)
                json.takeIf { it.isNotBlank() }?.let { gson.jsonToObjectOrNull(json, returnType) }
            }
        }
        @Suppress("UNCHECKED_CAST")
        return (value as? T)
    }

    private val config: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance().apply {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(CONFIG_CACHE_EXPIRATION_SECONDS)
            .build()
        setConfigSettingsAsync(configSettings)
        fetch(CONFIG_CACHE_EXPIRATION_SECONDS)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    activate()
                }
            }
        setDefaultsAsync(R.xml.remote_config_defaults)
    }

    private inline fun <reified T> read(param: ConfigParam): T? = read(param, T::class.java)

    override fun getPrompt(): String {
        return read<String>(ConfigParam.PROMPT) ?: SOME_DEFAULT_VALUE
    }

    override fun getModel(): String {
        return read<String>(ConfigParam.MODEL) ?: SOME_DEFAULT_VALUE
    }

    private companion object {
        /**
         * Config expiration interval 30 minutes.
         */
        private const val CONFIG_CACHE_EXPIRATION_SECONDS = 900L
        private const val SOME_DEFAULT_VALUE = "Any default value"
    }
}