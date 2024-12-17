package mock.enums;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum EnumClientStatus {
    @JsonAlias("TryLogout")
    eTryLogout, // 尝试登陆
    @JsonAlias("Offline")
    eOffline, // 登出
    @JsonAlias("Online")
    eOnline, // 登陆成功并在拉流
    eGetNeighborSucc,
    ePrtOffline,
    ePrtOnline,

    @JsonAlias("TryLogin")
    eTryToLogin
}
