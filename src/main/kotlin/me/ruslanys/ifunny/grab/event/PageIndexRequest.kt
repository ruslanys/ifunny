package me.ruslanys.ifunny.grab.event

import me.ruslanys.ifunny.channel.Channel

class PageIndexRequest(channel: Channel, val pageNumber: Int) : GrabEvent(channel)
