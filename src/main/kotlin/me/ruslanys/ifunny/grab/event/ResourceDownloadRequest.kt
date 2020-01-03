package me.ruslanys.ifunny.grab.event

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.MemeInfo

class ResourceDownloadRequest(channel: Channel, val info: MemeInfo) : GrabEvent(channel)
