

package stcam.stcamproject.network.api;

import com.model.RetModel;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by GYL on 11/22/16.
 */

public interface ServerCommandApi
{
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

  @GET("app_user_del_dev.asp")
  Observable<com.model.RetModel> app_user_del_dev(@Query("user") String user,
                                                  @Query("psd") String psd,
                                                  @Query("tokenid") String tokenid,
                                                  @Query("sn") String sn,
                                                  @Query("s") int s
  );

  @GET("app_user_get_devlst.asp")
  Observable<List<com.model.DevModel>> app_user_get_devlst(@Query("user") String user,
                                                           @Query("psd") String psd,
                                                           @Query("tokenid") String tokenid
  );

  @GET("app_user_check_usable.asp")
  Observable<com.model.RetModel> app_user_check_usable(@Query("user") String user
  );

  @GET("app_user_reg_send_verifycode.asp")
  Observable<com.model.RetModel> app_user_reg_send_verifycode(@Query("user") String user
  );


  @GET("app_user_forgotpsd_send_verifycode.asp")
  Observable<com.model.RetModel> app_user_forgotpsd_send_verifycode(@Query("user") String user
  );

  @GET("app_user_login.asp")
  Observable<com.model.RetModel> app_user_login(@Query("user") String user,
                                                @Query("psd") String psd,
                                                @Query("tokenid") String tokenid,
                                                @Query("mbtype") int mbtype,
                                                @Query("apptype") int apptype,
                                                @Query("pushtype") int pushtype
  );

  @GET("app_user_reg.asp")
  Observable<com.model.RetModel> app_user_reg(@Query("user") String user,
                                              @Query("psd") String psd,
                                              @Query("verifycode") String verifycode
  );

  @GET("app_user_forgotpsd.asp")
  Observable<com.model.RetModel> app_user_forgotpsd(@Query("user") String user,
                                                    @Query("newpsd") String psd,
                                                    @Query("verifycode") String verifycode
  );

  @GET("app_user_changepsd.asp")
  Observable<com.model.RetModel> app_user_changepsd(@Query("user") String user,
                                                    @Query("psd") String psd,
                                                    @Query("tokenid") String tokenid,
                                                    @Query("newpsd") String newpsd
  );

  @GET("app_share_get_lst.asp")
  Observable<List<com.model.UserModel>> app_share_get_lst(@Query("user") String user,
                                                          @Query("psd") String psd,
                                                          @Query("tokenid") String tokenid
  );

  @GET("app_share_del_sub.asp")
  Observable<com.model.RetModel> app_share_del_sub(@Query("user") String user,
                                                   @Query("psd") String psd,
                                                   @Query("tokenid") String tokenid,
                                                   @Query("sub") String sub
  );

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

  @GET("app_upgrade_app_check.asp")
  Observable<com.model.UpdateModel> app_upgrade_app_check(@Query("apptype") int apptype,
                                                          @Query("ostype") String ostype
  );

  @GET("app_upgrade_dev_check.asp")
  Observable<com.model.UpdateDevModel> app_upgrade_dev_check(@Query("devtype") int devtype
  );

  @GET("app_user_sn_getalmfilelst.asp")
  Observable<List<com.model.AlarmImageModel>> app_user_sn_getalmfilelst(@Query("user") String user,
                                                                        @Query("psd") String psd,
                                                                        @Query("tokenid") String tokenid,
                                                                        @Query("sn") String sn,
                                                                        @Query("dt") String dt,
                                                                        @Query("line") int line,
                                                                        @Query("page") int page

  );

  @GET("app_user_getalmfilelst.asp")
  Observable<List<com.model.AlarmImageModel>> app_user_getalmfilelst(@Query("user") String user,
                                                                     @Query("psd") String psd,
                                                                     @Query("tokenid") String tokenid,
                                                                     @Query("dt") String dt,
                                                                     @Query("line") int line,
                                                                     @Query("page") int page

  );


  @GET("app_user_delalmfile.asp")
  Observable<RetModel> app_user_delalmfile(@Query("user") String user,
                                           @Query("psd") String psd,
                                           @Query("tokenid") String tokenid,
                                           @Query("id") String id

  );


}
