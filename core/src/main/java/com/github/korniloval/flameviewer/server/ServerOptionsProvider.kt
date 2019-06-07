package com.github.korniloval.flameviewer.server

interface ServerOptionsProvider {
    fun getServerOptions(): ServerOptions
}

class ServerOptionsProviderImpl(var options: ServerOptions) : ServerOptionsProvider {
    override fun getServerOptions() = options
}
