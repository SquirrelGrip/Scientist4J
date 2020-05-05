package com.github.squirrelgrip.scientist4k.http.core.model

import com.google.common.net.MediaType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ExperimentResponseKtTest {

    @Test
    fun isTextLike() {
        assertThat(MediaType.PLAIN_TEXT_UTF_8.isTextLike()).isTrue()
        assertThat(MediaType.CSV_UTF_8.isTextLike()).isTrue()
        assertThat(MediaType.HTML_UTF_8.isTextLike()).isTrue()
        assertThat(MediaType.JSON_UTF_8.isTextLike()).isTrue()
        assertThat(MediaType.XHTML_UTF_8.isTextLike()).isTrue()

        assertThat(MediaType.AAC_AUDIO.isTextLike()).isFalse()
        assertThat(MediaType.GZIP.isTextLike()).isFalse()
    }
}