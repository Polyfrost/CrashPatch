package org.polyfrost.crashpatch.test

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.polyfrost.crashpatch.client.crashes.data.CrashData

class CrashDataTest {

    @Test
    fun `crash data decodes`() {
        val json = JsonParser.parseString(
            """{"fixtypes": [{"name": "Solutions"}], "default_fix_type": 0,
               "fixes": [{"fix": "Update OptiFine", "causes": [{"method": "contains", "value": "optifine"}]}]}""",
        )
        val data = CrashData.CODEC.parse(JsonOps.INSTANCE, json).result().orElse(null)

        Assertions.assertNotNull(data)
        Assertions.assertEquals(0, data!!.resolveId(data.fixes.single()))
        Assertions.assertTrue(data.fixes.single().triggersOn("at optifine.Foo"))
    }
}
