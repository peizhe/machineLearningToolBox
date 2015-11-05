package com.ireader.util.local;

import java.io.DataOutputStream;

/**
 * Created by zxsted on 15-8-20.
 *
 * 可写入或读取二进制
 */
public interface ICacheAble {

    /**/

    public void save(DataOutputStream out) throws Exception;

    public boolean load(ByteArray byteArray);

}
