package com.quantatw.sls.json;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.base.BaseResPack;


public class ExangeJson<T extends BaseResPack> {

	static private String Tag = "ExangeJson";
	@SuppressWarnings("unchecked")
	public  T Exe(String ret,T resPack)
	{
		Gson gson = new GsonBuilder().create();
		if (ret == null) {

			resPack.setStatus_code(ErrorKey.ConnectionError);
			
		} else {
			
			try {
				resPack =  (T) gson.fromJson(ret, resPack.getClass());

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				resPack.setStatus_code(ErrorKey.JsonError);
			}
			
	

		}
		return resPack;
	}
}
