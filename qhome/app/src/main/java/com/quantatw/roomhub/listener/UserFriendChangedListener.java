package com.quantatw.roomhub.listener;

import com.quantatw.sls.device.FriendData;

/**
 * Created by cherry.yang on 2015/12/24.
 */
public interface UserFriendChangedListener {
    public void AddFriend(FriendData friend_data);
    public void UpdateFriend(FriendData friend_data);
    public void RemoveFriend(FriendData friend_data);
}
