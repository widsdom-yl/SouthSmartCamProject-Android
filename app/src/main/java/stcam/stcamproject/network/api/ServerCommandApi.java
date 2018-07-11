

package stcam.stcamproject.network.api;

        import com.model.RetModel;

        import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by GYL on 11/22/16.
 */

public interface ServerCommandApi {


    /**
     * App 端设备列表中添加设备
     * http://xxx.xxx.xxx.xxx:800/app_user_add_dev.asp?user=4719373@qq.com&psd=admin111
     * &tokenid=100d85590943116f4c7&mbtype=1&apptype=0&pushtype=0&sn=80007604&s=117
     * @return
     * 成功返回{"ret":1} 失败返回{"ret":0} 设备已绑定返回{"ret":-4}
     */
    @GET("app_user_add_dev.asp")
    Observable<com.model.RetModel> app_user_add_dev(@Query("user") String user,
                                                    @Query("psd") String psd,
                                                    @Query("tokenid") String tokenid,
                                                    @Query("mbtype") int mbtype,
                                                    @Query("apptype") int apptype,
                                                    @Query("pushtype") int pushtype,
                                                    @Query("sn") String sn,
                                                    @Query("s") int s
    );


    /**
     * App 端设备列表中删除设备
     * @return
     * 成功返回{"ret":1} 失败返回{"ret":0}
     */
    @GET("app_user_del_dev.asp")
    Observable<com.model.RetModel> app_user_del_dev(@Query("user") String user,
                                                    @Query("psd") String psd,
                                                    @Query("sn") String sn,
                                                    @Query("s") int s
    );

    /**
     * App 端获取用户设备列表
     * @return
     * 失败返回[] 成功返回 jsonarray
     */
    @GET("app_user_get_devlst.asp")
    Observable<List<com.model.DevModel>> app_user_get_devlst(@Query("user") String user,
                                                       @Query("psd") String psd
    );

    /**
     * 检查帐号是否可用
     * @return
     * 帐号可用返回 json {"ret":1} 帐号不可用返回 json {"ret":0}
     */
    @GET("app_user_del_dev.asp")
    Observable<com.model.RetModel> app_user_check_usable(@Query("user") String user
    );

    /**
     * 发送注册验证码
     * @return
     * 成功返回 json 验证码{"ret":9843} 失败返回 json {"ret":0} 用户已存在返回 json {"ret":-1}
     */
    @GET("app_user_reg_send_verifycode.asp")
    Observable<com.model.RetModel> app_user_reg_send_verifycode(@Query("user") String user
    );

    /**
     * 用户登录
     * @return
     * 成功返回 json {"ret":1} 失败返回 json {"ret":0}
     */
    @GET("app_user_login.asp")
    Observable<com.model.RetModel> app_user_login(@Query("user") String user,
                                                  @Query("psd") String psd
    );

    /**
     * 用户注册
     * @return
     * 成功返回 json {"ret":1} 失败返回 json {"ret":0} 用户已存在返回 json {"ret":-1} 校验码错误返回 json {"ret":-2} 校验码超时返回 json {"ret":-3}
     */
    @GET("app_user_reg.asp")
    Observable<com.model.RetModel> app_user_reg(@Query("user") String user,
                                                  @Query("psd") String psd,
                                                  @Query("verifycode") String verifycode
    );

    /**
     * 用户忘记密码，发送密码到邮箱
     * @return
     * 成功返回 json {"ret":1} 失败返回 json {"ret":0}
     */
    @GET("app_user_forgotpsd.asp")
    Observable<com.model.RetModel> app_user_forgotpsd(@Query("user") String user
    );



    /**
     * 用户修改密码
     * @return
     * 成功返回 json {"ret":1} 失败返回 json {"ret":0} 用户已存在返回 json {"ret":-1} 校验码错误返回 json {"ret":-2} 校验码超时返回 json {"ret":-3}
     */
    @GET("app_user_changepsd.asp")
    Observable<com.model.RetModel> app_user_changepsd(@Query("user") String user,
                                                @Query("psd") String psd,
                                                @Query("newpsd") String verifycode
    );



    /**
     * 主帐号获取分享帐号列表
     * @return
     * 失败返回[] 成功返回 jsonarray
     */
    @GET("app_share_get_lst.asp")
    Observable<List<com.model.UserModel>> app_share_get_lst(@Query("user") String user,
                                                             @Query("psd") String psd
    );


    /**
     * 主帐号删除子帐号
     * @return
     * 成功返回 json {"ret":1} 失败返回 json {"ret":0}
     */
    @GET("app_share_del_sub.asp")
    Observable<com.model.RetModel> app_share_del_sub(@Query("user") String user,
                                                     @Query("psd") String psd,
                                                     @Query("sub") String sub
    );

    /**
     * 子帐号添加分享设备
     *
     * @return
     * 成功返回{"ret":1} 失败返回{"ret":0} 设备已绑定返回{"ret":-4}
     */
    @GET("app_share_add_dev.asp")
    Observable<com.model.RetModel> app_share_add_dev(@Query("user") String user,
                                                    @Query("psd") String psd,
                                                     @Query("from") String from,
                                                    @Query("tokenid") String tokenid,
                                                    @Query("mbtype") int mbtype,
                                                    @Query("apptype") int apptype,
                                                    @Query("pushtype") int pushtype,
                                                    @Query("sn") String sn,
                                                     @Query("video") int video,
                                                     @Query("history") int history,
                                                     @Query("push") int push,
                                                     @Query("setup") int setup,
                                                     @Query("control") int control
    );



    /**
     * App 端检查 App 更新
     * @return
     * 失败返回[] 成功返回 jsonarray
     */
    @GET("app_upgrade_app_check.asp")
    Observable<com.model.UpdateModel> app_upgrade_app_check(@Query("apptype") int apptype,
                                                            @Query("ostype") String ostype
    );

    /**
     * App 端检查设备更新
     * @return
     * 失败返回 json {"ret":0}
    成功返回 json
    {
    "ver":"V4.269.1234", "url":"http://xxx.xxx.xxx.xxx/aaaaaaa.bin", "crc":34884242343,
    "memo":"修改了 xxxxxx" }
     */
    @GET("app_upgrade_dev_check.asp")
    Observable<com.model.UpdateDevModel> app_upgrade_dev_check(@Query("devtype") int devtype
    );

    /**
     *App 端获取单一设备警报图片列表
     * @return
     * 失败返回[] 成功返回 jsonarray
     */
    @GET("app_user_getalmfilelst.asp")
    Observable<List<com.model.AlarmImageModel>> app_user_sn_getalmfilelst(@Query("user") String user,
                                                             @Query("psd") String psd,
                                                                       @Query("sn") String sn,
                                                                       @Query("dt") String dt,
                                                                               @Query("line") int line,
                                                                               @Query("page") int page

    );

    @GET("app_user_getalmfilelst.asp")
    Observable<List<com.model.AlarmImageModel>> app_user_getalmfilelst(@Query("user") String user,
                                                                          @Query("psd") String psd,
                                                                          @Query("dt") String dt,
                                                                          @Query("line") int line,
                                                                          @Query("page") int page

    );



    @GET("app_user_delalmfile.asp")
    Observable<RetModel> app_user_delalmfile(@Query("user") String user,
                                             @Query("psd") String psd,
                                             @Query("id") String id

    );


}
