package d3e.core;

import classes.LoginResult;
import classes.VerificationDataByToken;
import javax.annotation.PostConstruct;
import lists.VerificationDataByTokenImpl;
import models.AnonymousUser;
import models.BaseUser;
import models.OneTimePassword;
import models.VerificationData;
import models.VerificationDataByTokenRequest;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.jpa.AnonymousUserRepository;
import repository.jpa.AvatarRepository;
import repository.jpa.BaseUserRepository;
import repository.jpa.BaseUserSessionRepository;
import repository.jpa.OneTimePasswordRepository;
import repository.jpa.ReportConfigOptionRepository;
import repository.jpa.ReportConfigRepository;
import repository.jpa.VerificationDataRepository;
import security.AppSessionProvider;
import security.JwtTokenUtil;

@Service
public class QueryProvider {
  public static QueryProvider instance;
  @Autowired private JwtTokenUtil jwtTokenUtil;
  @Autowired private AnonymousUserRepository anonymousUserRepository;
  @Autowired private AvatarRepository avatarRepository;
  @Autowired private BaseUserRepository baseUserRepository;
  @Autowired private BaseUserSessionRepository baseUserSessionRepository;
  @Autowired private OneTimePasswordRepository oneTimePasswordRepository;
  @Autowired private ReportConfigRepository reportConfigRepository;
  @Autowired private ReportConfigOptionRepository reportConfigOptionRepository;
  @Autowired private VerificationDataRepository verificationDataRepository;
  @Autowired private VerificationDataByTokenImpl verificationDataByTokenImpl;
  @Autowired private ObjectFactory<AppSessionProvider> provider;

  @PostConstruct
  public void init() {
    instance = this;
  }

  public static QueryProvider get() {
    return instance;
  }

  public AnonymousUser getAnonymousUserById(long id) {
    return anonymousUserRepository.findById(id);
  }

  public OneTimePassword getOneTimePasswordById(long id) {
    return oneTimePasswordRepository.findById(id);
  }

  public boolean checkTokenUniqueInOneTimePassword(long oneTimePassword_id, String token) {
    return oneTimePasswordRepository.checkTokenUnique(oneTimePassword_id, token);
  }

  public VerificationData getVerificationDataById(long id) {
    return verificationDataRepository.findById(id);
  }

  public VerificationDataByToken getVerificationDataByToken(VerificationDataByTokenRequest inputs) {
    return verificationDataByTokenImpl.get(inputs);
  }

  public LoginResult loginWithOTP(String token, String code, String deviceToken) {
    OneTimePassword otp = oneTimePasswordRepository.getByToken(token);
    BaseUser user = otp.getUser();
    LoginResult loginResult = new LoginResult();
    if (deviceToken != null) {
      user.setDeviceToken(deviceToken);
    }
    loginResult.setSuccess(true);
    loginResult.setUserObject(otp.getUser());
    loginResult.setToken(token);
    return loginResult;
  }
}
