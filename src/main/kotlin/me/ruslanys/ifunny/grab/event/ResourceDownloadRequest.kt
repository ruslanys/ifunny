package me.ruslanys.ifunny.grab.event

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.MemeInfo

class ResourceDownloadRequest(channel: Channel, val info: MemeInfo) : GrabEvent(channel) {
    override fun toString(): String {
        return "ResourceDownloadRequest(channel=$channel, info=$info)"
    }
}
