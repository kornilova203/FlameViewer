package com.github.korniloval.flameviewer.server

import okhttp3.HttpUrl
import org.jetbrains.ide.BuiltInServerManager

fun getUrlBuilderBase(): HttpUrl.Builder = HttpUrl.Builder()
        .scheme("http")
        .host("localhost")
        .port(BuiltInServerManager.getInstance().port)
        .addPathSegments(NAME)
