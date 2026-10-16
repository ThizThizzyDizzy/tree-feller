package com.thizthizzydizzy.treefeller.core.test.options.tools;
import com.thizthizzydizzy.treefeller.core.test.FixtureOptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
public final class TreesTests{
    private TreesTests(){}
    public static void register(ITestPlatform platform){
        FixtureOptionTestSuite tests = new FixtureOptionTestSuite("tools.trees");
        TestTrees.Tree spruce = TestTrees.specimen(TestTrees.Kind.SPRUCE, 4);
        TestTrees.Tree oak = TestTrees.specimen(TestTrees.Kind.OAK);
        add(tests, "whitelist-match", "whitelist", spruce, true);
        add(tests, "whitelist-mismatch", "whitelist", oak, false);
        add(tests, "blacklist-match", "blacklist", spruce, false);
        add(tests, "blacklist-mismatch", "blacklist", oak, true);
        tests.register(platform);
    }
    private static void add(FixtureOptionTestSuite tests, String name, String list, TestTrees.Tree tree, boolean allowed){
        String config = "trees = [{ trunk = [spruce_log], leaves = [spruce_leaves] }, { trunk = [oak_log], leaves = [oak_leaves] }]\n"
                +"tools = [{ item = { material = IRON_AXE }, trees = { "+list+" = [0] } }]\n";
        FixtureOptionTestSuite.Fixture fixture = tests.add(name, config);
        fixture.tree = tree;
        fixture.allowed = allowed;
    }
}
