package com.github.korniloval.flameviewer.server

interface ServerOptionsProvider {
    fun opt(): ServerOptions
}

class ServerOptionsProviderImpl(var options: ServerOptions) : ServerOptionsProvider {
    override fun opt() = options
}
