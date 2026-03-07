package com.thizthizzydizzy.treefeller.platform.utility.version;
import java.util.ArrayList;
import java.util.HashMap;
public class VersionMatcher<T>{
    public static class VersionMatcherBuilder{
        private final VersionType type;
        private VersionMatcherBuilder(VersionType type){
            this.type = type;
        }
        public <T> VersionMatcher<T> ascending(T oldVersionDefault){
            VersionMatcher<T> matcher = new VersionMatcher<>(type, InterpolationMode.ASCENDING);
            return matcher.atVersion(null, oldVersionDefault);
        }
        public <T> VersionMatcher<T> descending(T newVersionDefault){
            VersionMatcher<T> matcher = new VersionMatcher<>(type, InterpolationMode.DESCENDING);
            return matcher.atVersion(null, newVersionDefault);
        }
        public <T> VersionMatcher<T> exact(){
            return new VersionMatcher<>(type, InterpolationMode.EXACT);
        }
    }
    private final VersionType versionType;
    private final InterpolationMode interpolationMode;
    private final HashMap<String, T> values = new HashMap();
    private VersionMatcher(VersionType versionType, InterpolationMode mode){
        this.versionType = versionType;
        this.interpolationMode = mode;
    }
    public static VersionMatcherBuilder by(VersionType type){
        return new VersionMatcherBuilder(type);
    }
    public VersionMatcher<T> atVersion(String version, T value){
        values.put(version, value);
        return this;
    }
    public T match(String version){
        if(values.containsKey(version))return values.get(version);
        ArrayList<String> versions = new ArrayList<>();
        versions.addAll(values.keySet());
        versions.add(version);
        versions.remove(null);

        versions.sort(versionType::sort);

        T value = values.get(null);
        switch(interpolationMode){
            case ASCENDING:
                for(int i = 0; i<versions.size(); i++){
                    String v = versions.get(i);
                    if(values.containsKey(v)){
                        T val = values.get(v);
                        value = val;
                    }
                    if(version.equals(v))return value;
                }
                break;
            case DESCENDING:
                for(int i = versions.size()-1; i>=0; i--){
                    String v = versions.get(i);
                    if(values.containsKey(v)){
                        T val = values.get(v);
                        value = val;
                    }
                    if(version.equals(v))return value;
                }
                break;
            case EXACT:
                // This would have already been found, don't need to do anything
                break;
            default:
                throw new AssertionError("Unknown interpolation mode: "+interpolationMode.name());
        }
        throw new RuntimeException("No match found for version: "+version+" ("+interpolationMode.toString()+")");
    }
    public static enum InterpolationMode{
        ASCENDING,
        DESCENDING,
        EXACT
    }
    public static enum VersionType{
        MINECRAFT{
            @Override
            int sort(String version1, String version2){
                String[] parts1 = version1.split("\\.");
                String[] parts2 = version2.split("\\.");

                int length = Math.max(parts1.length, parts2.length);

                for(int i = 0; i<length; i++){
                    int v1 = i<parts1.length?Integer.parseInt(parts1[i]):0;
                    int v2 = i<parts2.length?Integer.parseInt(parts2[i]):0;

                    if(v1<v2)return -1;
                    if(v1>v2)return 1;
                }

                return 0;
            }
        };
        abstract int sort(String version1, String version2);
    }
}
