package com.quantatw.sls.listener;

import com.quantatw.sls.pack.account.UserFriendResPack;

public interface UserFriendListener {
	public void addFriend(UserFriendResPack friend);
	public void updateFriend(UserFriendResPack friend);
	public  void removeFriend(UserFriendResPack friend);
}
