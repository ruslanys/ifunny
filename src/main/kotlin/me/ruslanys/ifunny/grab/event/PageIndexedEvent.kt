package me.ruslanys.ifunny.grab.event

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.Page

class PageIndexedEvent(channel: Channel, val page: Page) : GrabEvent(channel) {
    override fun toString(): String {
        return "PageIndexedEvent(channel=${channel.getName()}, page=$page)"
    }
}
