package me.ruslanys.ifunny.grab.event

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.Page

class PageIndexedEvent(channel: Channel, val page: Page, val new: Int) : GrabEvent(channel) {
    override fun toString(): String {
        return "PageIndexedEvent(channel=$channel, page=$page, new=$new)"
    }
}
