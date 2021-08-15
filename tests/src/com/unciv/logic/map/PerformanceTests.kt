package com.unciv.logic.map

import com.unciv.UncivGame
import com.unciv.logic.GameInfo
import com.unciv.logic.GameSaver
import com.unciv.models.ruleset.RulesetCache
import com.unciv.testing.GdxTestRunner
import com.unciv.ui.utils.CrashController
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(GdxTestRunner::class)
class PerformanceTests {
    private var gameInfo = GameInfo()

    @Before
    fun initTheWorld() {
        RulesetCache.loadRulesets()
        UncivGame.Current = UncivGame("")
        UncivGame.Current.settings = GameSaver.getGeneralSettings()
        //UncivGame.Current.crashController = CrashController.Impl(UncivGame.Current.crashReportSender)
        gameInfo = GameSaver.loadGameByName("The_Ottomans_-_401_turns_Lag")
        UncivGame.Current.gameInfo = gameInfo
    }

    @Test
    fun nextTurnPerformance() {
        val turns = 10

        val runtime = Runtime.getRuntime()
        val startFreeMem = runtime.freeMemory()
        val startTime = System.nanoTime()
        doNextTurn(turns)
        val endTime = System.nanoTime()
        val endFreeMem = runtime.freeMemory()

        println("${turns}x next turn took ${(endTime-startTime)/1_000_000_000f}s and ${(startFreeMem-endFreeMem)/1024}kB")
    }

    private fun doNextTurn(turns: Int = 1) {
        repeat(turns) {
            gameInfo.nextTurn()
        }
    }
}