package com.zhonghe.kernel.model;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Date;

/**
 * C格式化类似于'2017-04-26T23:09:47'格式的时间
 */
public class DateTimeDeserializer extends JsonDeserializer<Date>{

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String str = jsonParser.getText().trim();
        str = str.replace("T", " ");
        Date deserializeDate = new Date();
        try{
            if (StrUtil.isBlank(str)){
                return null;
            }
            deserializeDate = DateUtil.parse(str);
            return deserializeDate;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
