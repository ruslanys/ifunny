package me.ruslanys.ifunny.grab.event

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.Page

class PageIndexSuccessful(channel: Channel, val page: Page, val new: Int) : PageIndexEvent(channel) {
    override fun toString(): String {
        return "PageIndexSuccessful(channel=$channel, page=$page, new=$new)"
    }
}
