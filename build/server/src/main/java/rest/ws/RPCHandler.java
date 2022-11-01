package rest.ws;

import classes.FileService;
import d3e.core.DFile;
import d3e.core.RPCConstants;
import d3e.core.UniqueChecker;
import models.OneTimePassword;
import org.springframework.stereotype.Service;

@Service
public class RPCHandler {
  public void handle(int clsIdx, int methodIdx, RocketInputContext ctx, rest.ws.RocketMessage msg) {
    switch (clsIdx) {
      case RPCConstants.UniqueChecker:
        {
          handleUniqueChecker(methodIdx, ctx, msg);
          break;
        }
      case RPCConstants.FileService:
        {
          handleFileService(methodIdx, ctx, msg);
          break;
        }
    }
  }

  private void handleUniqueChecker(
      int methodIdx, RocketInputContext ctx, rest.ws.RocketMessage msg) {
    switch (methodIdx) {
      case RPCConstants.UniqueCheckerCheckTokenUniqueInOneTimePassword:
        {
          OneTimePassword oneTimePassword = ctx.readObject();
          String token = ctx.readString();
          boolean result = UniqueChecker.checkTokenUniqueInOneTimePassword(oneTimePassword, token);
          msg.writeByte(0);
          ctx.writeBoolean(result);
          break;
        }
    }
  }

  private void handleFileService(int methodIdx, RocketInputContext ctx, rest.ws.RocketMessage msg) {
    switch (methodIdx) {
      case RPCConstants.FileServiceCreateTempFile:
        {
          String fullNameOrExtn = ctx.readString();
          boolean extnGiven = ctx.readBoolean();
          String content = ctx.readString();
          DFile result = FileService.createTempFile(fullNameOrExtn, extnGiven, content);
          msg.writeByte(0);
          ctx.writeDFile(result);
          break;
        }
    }
  }
}
