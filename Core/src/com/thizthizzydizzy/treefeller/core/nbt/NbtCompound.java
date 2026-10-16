package com.thizthizzydizzy.treefeller.core.nbt;
import java.io.*;
import java.util.*;
/** Typed, platform-independent NBT matching and compound merging. */
public final class NbtCompound{
    private final Tag root;
    private NbtCompound(Tag root){this.root=root;}
    public static NbtCompound read(byte[] bytes){
        try{
            DataInputStream in=new DataInputStream(new ByteArrayInputStream(bytes));
            if(in.readUnsignedByte()!=10)throw new IOException("Expected an NBT compound");
            in.readUTF();
            return new NbtCompound(readTag(in,10,0));
        }catch(IOException ex){throw new IllegalArgumentException("Invalid NBT compound",ex);}
    }
    public byte[] bytes(){
        try{
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            DataOutputStream out=new DataOutputStream(bytes);
            out.writeByte(10);out.writeUTF("");writeTag(out,root);
            return bytes.toByteArray();
        }catch(IOException ex){throw new IllegalStateException(ex);}
    }
    public boolean isEmpty(){return root.compound().isEmpty();}
    public NbtCompound child(String key){
        Tag child=root.compound().get(key);
        return new NbtCompound(child!=null&&child.type==10?child:new Tag(10,new LinkedHashMap<String,Tag>()));
    }
    public NbtCompound withChild(String key,NbtCompound child){
        Map<String,Tag> result=new LinkedHashMap<>(root.compound());result.put(key,child.root);
        return new NbtCompound(new Tag(10,result));
    }
    /** Block placement cannot change the native identity or position. */
    public NbtCompound preserving(NbtCompound original,String... keys){
        Map<String,Tag> result=new LinkedHashMap<>(root.compound());
        for(String key:keys){Tag value=original.root.compound().get(key);if(value==null)result.remove(key);else result.put(key,value);}
        return new NbtCompound(new Tag(10,result));
    }
    /** Compound keys are a subset; lists use Minecraft's unordered subset matching. */
    public boolean matches(NbtCompound actual){return matches(root,actual.root);}
    public NbtCompound merge(NbtCompound patch){return new NbtCompound(merge(root,patch.root));}
    private static Tag merge(Tag original,Tag patch){
        if(original.type!=10||patch.type!=10)return patch;
        Map<String,Tag> result=new LinkedHashMap<>(original.compound());
        patch.compound().forEach((key,value) -> result.merge(key,value,NbtCompound::merge));
        return new Tag(10,result);
    }
    private static boolean matches(Tag expected,Tag actual){
        if(actual==null||expected.type!=actual.type)return false;
        if(expected.type==10){
            for(Map.Entry<String,Tag> entry:expected.compound().entrySet())
                if(!matches(entry.getValue(),actual.compound().get(entry.getKey())))return false;
            return true;
        }
        if(expected.type==9){
            List<Tag> wanted=expected.list(),found=actual.list();
            if(wanted.isEmpty())return found.isEmpty();
            for(Tag tag:wanted)if(found.stream().noneMatch(value -> matches(tag,value)))return false;
            return true;
        }
        return expected.value.equals(actual.value);
    }
    private static int length(DataInput in)throws IOException{
        int length=in.readInt();
        if(length<0||length>16777216)throw new IOException("Invalid NBT collection length: "+length);
        return length;
    }
    private static Tag readTag(DataInput in,int type,int depth)throws IOException{
        if(depth>512)throw new IOException("NBT nesting exceeds 512");
        Object value;
        switch(type){
            case 1:value=in.readByte();break;
            case 2:value=in.readShort();break;
            case 3:value=in.readInt();break;
            case 4:value=in.readLong();break;
            case 5:value=in.readFloat();break;
            case 6:value=in.readDouble();break;
            case 8:value=in.readUTF();break;
            case 7:case 11:case 12:{
                int size=length(in);List<Object> array=new ArrayList<>(size);
                for(int i=0;i<size;i++)array.add(type==7?(Object)in.readByte():type==11?(Object)in.readInt():in.readLong());
                value=array;break;
            }
            case 9:{
                int element=in.readUnsignedByte(),size=length(in);
                List<Tag> list=new ArrayList<>(size);
                for(int i=0;i<size;i++)list.add(readTag(in,element,depth+1));
                value=list;break;
            }
            case 10:{
                Map<String,Tag> compound=new LinkedHashMap<>();
                int child;
                while((child=in.readUnsignedByte())!=0){String name=in.readUTF();compound.put(name,readTag(in,child,depth+1));}
                value=compound;break;
            }
            default:throw new IOException("Unknown NBT type: "+type);
        }
        return new Tag(type,value);
    }
    private static void writeTag(DataOutput out,Tag tag)throws IOException{
        switch(tag.type){
            case 1:out.writeByte((Byte)tag.value);break;
            case 2:out.writeShort((Short)tag.value);break;
            case 3:out.writeInt((Integer)tag.value);break;
            case 4:out.writeLong((Long)tag.value);break;
            case 5:out.writeFloat((Float)tag.value);break;
            case 6:out.writeDouble((Double)tag.value);break;
            case 8:out.writeUTF((String)tag.value);break;
            case 7:case 11:case 12:{
                List<?> array=(List<?>)tag.value;out.writeInt(array.size());
                for(Object value:array){if(tag.type==7)out.writeByte((Byte)value);else if(tag.type==11)out.writeInt((Integer)value);else out.writeLong((Long)value);}
                break;
            }
            case 9:{
                List<Tag> list=tag.list();out.writeByte(list.isEmpty()?0:list.get(0).type);out.writeInt(list.size());
                for(Tag child:list)writeTag(out,child);break;
            }
            case 10:
                for(Map.Entry<String,Tag> entry:tag.compound().entrySet()){
                    out.writeByte(entry.getValue().type);out.writeUTF(entry.getKey());writeTag(out,entry.getValue());
                }
                out.writeByte(0);break;
            default:throw new IOException("Unknown NBT type: "+tag.type);
        }
    }
    private static final class Tag{
        final int type;final Object value;
        Tag(int type,Object value){this.type=type;this.value=value;}
        @SuppressWarnings("unchecked") Map<String,Tag> compound(){return (Map<String,Tag>)value;}
        @SuppressWarnings("unchecked") List<Tag> list(){return (List<Tag>)value;}
    }
}
