package de.muenchen.wollmux.conf.service;

import java.util.Base64;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;

@DataObject
public class FileObject
{
  private String type;
  private String content;
  
  public FileObject(JsonObject json)
  {
    type = json.getString("type", "");
    content = json.getString("content", "");
  }
  
  public FileObject(String content, String type)
  {
    this.type = type;
    this.content = content;
  }
  
  public FileObject(byte[] content, String type)
  {
    this.type = type;
    setContent(content);
  }
  
  public JsonObject toJson() 
  {
    JsonObject ret = new JsonObject();
    ret.put("type", type);
    ret.put("content", content);
    
    return ret;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getContent()
  {
    return content;
  }

  @GenIgnore
  public byte[] getContentAsBytes()
  {
    if (content == null)
    {
      return null;
    }
    return Base64.getDecoder().decode(content);
  }

  @GenIgnore
  public void setContent(byte[] content)
  {
    if (content != null) 
    {
      this.content = Base64.getEncoder().encodeToString(content);
    }
  }
}
