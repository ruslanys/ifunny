package me.ruslanys.ifunny.grab.event

import me.ruslanys.ifunny.channel.Channel

class PageIndexRequest(channel: Channel, val pageNumber: Int) : GrabEvent(channel) {
    override fun toString(): String {
        return "PageIndexRequest(channel=$channel, pageNumber=$pageNumber)"
    }
}
