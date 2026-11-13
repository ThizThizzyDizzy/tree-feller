package com.thizthizzydizzy.treefeller.core.test.control;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.*;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.control.*;
import com.thizthizzydizzy.treefeller.core.player.PlayerSettings;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

public final class ControlTests{
    private static final String PLAYER=OptionTestSuite.PLAYER;
    private TestArena arena;
    private Boolean previousToggle;
    private final TestTrees.Tree tree=TestTrees.specimen(TestTrees.Kind.SPRUCE);
    private static final String BASE="debug.startup_logs=false\nglobal.trigger.cooldown=0\n";
    public static void register(ITestPlatform platform){new ControlTests().registerSuites(platform);}
    private void registerSuites(ITestPlatform hooks){
        OptionTestSuite commands=new OptionTestSuite("controls.commands").lifecycle(this::setup,this::finish,this::cleanup);
        commands.add("toggle",BASE,(c,h) -> {
            String[] actions={"off","on","toggle off","toggle on","toggle","toggle"};
            for(int i=0;i<actions.length;i++){
                final boolean expected=i%2==1;int x=i*8;
                command(c,h,actions[i],"toggle | "+actions[i]+" | expect "+(expected?"felling":"single log"));
                check(c,() -> equal(expected,PlayerSettings.get(h.playerState(PLAYER)).isToggledOn(),"Toggle state"));
                TestArena.onServer(c,() -> {h.preparePlayer(PLAYER,true);return null;});
                c.teleportPlayer(PLAYER,x+0.5,arena.floorY+1,-1.5,0.0,0.0);
                label(c,h,"toggle="+expected+" | cut spruce at x="+x);
                c.breakBlock(x,arena.floorY+1,2);
                if(expected)arena.assertLogsRemoved(tree,x,2);else arena.assertOnlyLogRemoved(tree,x,2,0,0,0);
            }
        });
        commands.add("permissions-and-help",BASE,(c,h) -> {
            ITestPlatform.TestOutputRecorder capture=TestArena.onServer(c,() -> h.recordOutput(PLAYER));
            Runnable remove=TestArena.onServer(c,() -> h.permissions(PLAYER,permissions(false)));
            try{
                check(c,() -> PlayerSettings.get(h.playerState(PLAYER)).setToggledOn(true));
                command(c,h,"off","treefeller.toggle=false | refuse toggle");
                check(c,() -> equal(true,PlayerSettings.get(h.playerState(PLAYER)).isToggledOn(),"Denied toggle unchanged"));
                command(c,h,"reload","treefeller.reload=false | refuse reload");
                command(c,h,"debug on","treefeller.debug=false | refuse debug");
                command(c,h,"config","treefeller.config=false | refuse editor");
                check(c,() -> equal(false,h.controlEntries(PLAYER).contains("global"),"Denied editor stays closed"));
                command(c,h,"help","treefeller.help=true | list permitted help");
                check(c,() -> {
                    List<String> messages=new ArrayList<>();for(ITestPlatform.MessageObservation m:capture.messages())messages.add(m.text);
                    equal(4L,messages.stream().filter(m -> m.equals("You do not have permission.")).count(),"Each denied capability reports refusal");
                    equal(true,messages.contains("/treefeller help"),"Help response");
                    equal(false,messages.contains("/treefeller config"),"Help filters permissions");
                    equal(false,TreeFellerCore.config.debug.chat,"Denied debug unchanged");
                    equal(Collections.singletonList("help"),TreeFellerCommands.complete(audience(h),new String[]{""}),"Permission filtered completion");
                });
            }finally{check(c,() -> {remove.run();capture.close();});}
            command(c,h,"toggle nonsense","invalid toggle | unchanged");
            check(c,() -> equal(true,PlayerSettings.get(h.playerState(PLAYER)).isToggledOn(),"Invalid toggle unchanged"));
            command(c,h,"help","help | all permitted capabilities");
            command(c,h,"nonsense","unknown command | usage");
            check(c,() -> equal(Collections.singletonList("toggle"),TreeFellerCommands.complete(audience(h),new String[]{"toggle"}),"Full prefix completion"));
        });
        commands.add("debug",BASE+"debug.console=true\n",(c,h) -> {
            for(String action:new String[]{"debug on","debug off","debug","debug"}){
                boolean expected=action.equals("debug on")||!TreeFellerCore.config.debug.chat&&action.equals("debug");
                command(c,h,action,action+" | chat="+expected);
                check(c,() -> {equal(expected,TreeFellerCore.config.debug.chat,"Debug chat state");equal(true,TreeFellerCore.config.debug.console,"Console setting preserved");});
            }
            command(c,h,"debug status","debug status | trace controls remain available");
            command(c,h,"debug clear","debug clear | clear trace");
        });
        commands.add("feedback",BASE+"messages.toggle_on {chat=\"Custom enabled\",actionbar=\"Enabled bar\"}\nmessages.toggle_off {chat=null,actionbar=null}\n",(c,h) -> {
            ITestPlatform.TestOutputRecorder capture=TestArena.onServer(c,() -> h.recordOutput(PLAYER));
            try{
                command(c,h,"on","messages.toggle_on=custom | chat and actionbar feedback");
                command(c,h,"off","messages.toggle_off=null | silent feedback");
                check(c,() -> {
                    List<ITestPlatform.MessageObservation> output=capture.messages();
                    equal(1L,output.stream().filter(m -> !m.actionbar&&m.text.equals("Custom enabled")).count(),"Custom toggle chat");
                    equal(1L,output.stream().filter(m -> m.actionbar&&m.text.equals("Enabled bar")).count(),"Custom toggle actionbar");
                    equal(false,output.stream().anyMatch(m -> m.text.equals("TreeFeller disabled.")),"Null feedback stays disabled");
                });
            }finally{check(c,capture::close);}
        });
        commands.register(hooks);
        OptionTestSuite editor=new OptionTestSuite("controls.config").lifecycle(this::setup,this::finish,(c,h) -> {check(c,() -> h.closeControl(PLAYER));cleanup(c,h);});
        editor.add("edit-save-reload",BASE,(c,h) -> configuration(c,h));
        editor.register(hooks);
    }
    private static Map<String,Boolean> permissions(boolean enabled){Map<String,Boolean> values=new HashMap<>();for(String key:new String[]{"toggle","reload","debug","config"})values.put("treefeller."+key,enabled);values.put("treefeller.help",true);return values;}
    private void setup(TestContext c,ITestPlatform h){
        previousToggle=TestArena.onServer(c,() -> PlayerSettings.get(h.playerState(PLAYER)).isToggledOn());
        arena=TestArena.setup(c,h,PLAYER,-4,-6,56,99);
        arena.spawnAll(tree,new int[][]{{0,2},{8,2},{16,2},{24,2},{32,2},{40,2}});
    }
    private void cleanup(TestContext c,ITestPlatform h){
        if(previousToggle!=null){
            check(c,() -> PlayerSettings.get(h.playerState(PLAYER)).setToggledOn(previousToggle));
            previousToggle=null;
        }
    }
    private void configuration(TestContext c,ITestPlatform h){
        Path path=TreeFellerCore.connector.configurationPath();byte[] original;
        try{original=Files.exists(path)?Files.readAllBytes(path):null;}catch(IOException ex){throw new IllegalStateException(ex);}
        try{
            command(c,h,"config","config | open editor");
            click(c,h,"global");click(c,h,"trigger");click(c,h,"default_enabled");
            check(c,() -> equal(false,TreeFellerCore.config.global.trigger.default_enabled,"Inventory boolean applies immediately"));
            click(c,h,"cooldown");submit(c,h,"7");
            check(c,() -> equal(7,TreeFellerCore.config.global.trigger.cooldown,"Numeric edit"));
            click(c,h,"cooldown");submit(c,h,"\"invalid\"");
            check(c,() -> equal(7,TreeFellerCore.config.global.trigger.cooldown,"Invalid edit leaves configuration intact"));
            click(c,h,"Save configuration");
            check(c,() -> {equal(7,ConfigReader.read(path).global.trigger.cooldown,"Saved numeric value");equal(false,ConfigReader.read(path).global.trigger.default_enabled,"Saved boolean");h.closeControl(PLAYER);});
            check(c,() -> TreeFellerCore.config.global.trigger.cooldown=99);
            label(c,h,"reload from console | no player context required");
            c.executeConsole("treefeller reload");c.waitTicks(5);
            check(c,() -> equal(7,TreeFellerCore.config.global.trigger.cooldown,"Console reload applied"));
            label(c,h,"toggle from console | safe player requirement");
            c.executeConsole("treefeller off");c.waitTicks(2);
            check(c,() -> TreeFellerCore.config.global.trigger.cooldown=99);
            command(c,h,"reload","reload | restore saved value");
            check(c,() -> equal(7,TreeFellerCore.config.global.trigger.cooldown,"Reload applied"));
            try{Files.write(path,"unknown_key = true".getBytes(StandardCharsets.UTF_8));}catch(IOException ex){throw new IllegalStateException(ex);}
            command(c,h,"reload","reload malformed config | retain live configuration");
            check(c,() -> equal(7,TreeFellerCore.config.global.trigger.cooldown,"Failed reload preserved live value"));
            command(c,h,"config","config | tools, trees, effects, messages");
            click(c,h,"tools");click(c,h,"Add entry");submit(c,h,"{item=diamond_axe}");
            check(c,() -> equal(true,TreeFellerCore.config.tools.length>0,"Tool added"));
            click(c,h,"Back");click(c,h,"messages");click(c,h,"toggle_on");click(c,h,"chat");submit(c,h,"\"Editor message\"");
            check(c,() -> equal("Editor message",TreeFellerCore.config.messages.toggle_on.chat,"Message edited"));
            click(c,h,"Back");click(c,h,"Back");click(c,h,"global");click(c,h,"result");click(c,h,"effects");
            // Nullable sections and values can be created through their typed configuration representation.
            submit(c,h,"[]");click(c,h,"effects");click(c,h,"Add entry");submit(c,h,"{type=SOUND,location=TOOL,sound=block.note_block.bell}");
            check(c,() -> equal(1,TreeFellerCore.config.global.result.effects.length,"Effect added"));
            click(c,h,"[0]");click(c,h,"volume");submit(c,h,"0.5");
            check(c,() -> equal(0.5f,TreeFellerCore.config.global.result.effects[0].volume,"Effect edited"));
            click(c,h,"Back");click(c,h,"[0]");click(c,h,"Reset / inherit");
            check(c,() -> equal(0,TreeFellerCore.config.global.result.effects.length,"Effect removed"));
            click(c,h,"Back");click(c,h,"Back");click(c,h,"Back");click(c,h,"trees");click(c,h,"Add entry");submit(c,h,"{trunk=[oak_log],leaves=[oak_leaves]}");
            check(c,() -> equal(true,TreeFellerCore.config.trees.length>0,"Tree added"));
            click(c,h,"Back");click(c,h,"global");click(c,h,"trigger");click(c,h,"cooldown");
            check(c,() -> {try{ConfigWriter.write(path,TreeFellerCore.config);}catch(IOException ex){throw new IllegalStateException(ex);}});
            command(c,h,"reload","reload while input pending | reject stale edit");
            submit(c,h,"23");
            check(c,() -> equal(7,TreeFellerCore.config.global.trigger.cooldown,"Stale edit refused"));
            click(c,h,"global");click(c,h,"trigger");click(c,h,"cooldown");
            Runnable revoke=TestArena.onServer(c,() -> h.permissions(PLAYER,Collections.singletonMap("treefeller.config",false)));
            try{submit(c,h,"99");check(c,() -> equal(7,TreeFellerCore.config.global.trigger.cooldown,"Revoked editor permission refused"));}
            finally{check(c,revoke);}
            check(c,() -> h.closeControl(PLAYER));
        }finally{
            try{if(original==null)Files.deleteIfExists(path);else Files.write(path,original);}catch(IOException ex){throw new IllegalStateException(ex);}
        }
    }
    private static void click(TestContext c,ITestPlatform h,String title){
        label(c,h,"config | "+title);
        TestArena.onServer(c,() -> {List<String> entries=h.controlEntries(PLAYER);int slot=entries.indexOf(title);if(slot<0)throw new AssertionError("Missing UI entry "+title+": "+entries);h.clickControl(PLAYER,slot);return null;});c.waitTicks(2);
    }
    private static void submit(TestContext c,ITestPlatform h,String text){label(c,h,"config value="+text);check(c,() -> h.submitControlText(PLAYER,text));c.waitTicks(2);}
    private static void command(TestContext c,ITestPlatform h,String args,String description){label(c,h,description);c.sendPlayerCommand("treefeller "+args);}
    private static void label(TestContext c,ITestPlatform h,String label){check(c,() -> h.showTestStatus(PLAYER,label));}
    private static void check(TestContext c,Runnable action){TestArena.onServer(c,() -> {action.run();return null;});}
    private static void equal(Object expected,Object actual,String label){if(!Objects.equals(expected,actual))throw new AssertionError(label+": expected "+expected+", got "+actual);}
    private static ControlAudience audience(ITestPlatform h){return new ControlAudience(){
        public boolean hasPermission(String permission){return h.playerState(PLAYER).hasPermission(permission);}
        public void sendMessage(String message){h.playerState(PLAYER).sendMessage(message);}
        public IPlayerConnector player(){return h.playerState(PLAYER);}
        public void show(Screen screen){throw new AssertionError("Unexpected screen");}
        public void requestText(String title,String initial,Consumer<String> submit,Runnable cancel){throw new AssertionError("Unexpected input");}
    };}
    private void finish(TestContext c,ITestPlatform h){check(c,() -> {h.closeControl(PLAYER);h.prepareCamera(PLAYER);});c.teleportPlayer(PLAYER,20.5,arena.floorY+20,-15.5,0.0,40.0);c.waitTicks(40);}
}
