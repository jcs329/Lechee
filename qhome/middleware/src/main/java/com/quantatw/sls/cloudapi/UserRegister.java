package com.quantatw.sls.cloudapi;

import android.util.Log;

import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.account.AccountReqPack;
import com.quantatw.sls.pack.account.AccountResPack;

public class UserRegister {

//	public AccountResPack UserRegisterREQ(AccountReqPack _AccountReqPack) {
//
//		
//		String data = gson.toJson(_AccountReqPack);
//		Log.d("CloudAPI", data);
//
//		String ret = PostBaseReq(API_USERREGISTER, data);
//		
//		if (ret == null) {
//			
//			AccountResPack resPack = new AccountResPack();
//			resPack.setStatus_code(ErrorKey.ConnectionError);
//			return resPack;
//		} else {
//			Log.d("CloudAPI","BaseReq Return: "+ ret);
//			AccountResPack resPack;
//			try {
//				resPack = gson.fromJson(ret, AccountResPack.class);
//
//			} catch (Exception e) {
//				// TODO: handle exception
//				resPack = new AccountResPack();
//				resPack.setStatus_code(ErrorKey.JsonError);
//			}
//			
//			return resPack;
//
//		}
//	}
}
