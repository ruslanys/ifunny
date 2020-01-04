package me.ruslanys.ifunny.base

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

/**
 * This class doing the same as <code>MockitoAnnotations.initMocks(this);</code>
 */
@ExtendWith(MockitoExtension::class)
abstract class ServiceTests
