package me.ruslanys.ifunny.service

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.MemeInfo
import me.ruslanys.ifunny.domain.Meme

interface MemeService {

    fun add(channel: Channel, info: MemeInfo): Meme

}
