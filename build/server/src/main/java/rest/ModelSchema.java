package rest;

import classes.ASyncType;
import classes.ArrayType;
import classes.ChangeEventType;
import classes.ColumnWidthType;
import classes.CommentType;
import classes.ConnectionStatus;
import classes.DBResult;
import classes.DBResultStatus;
import classes.IconType;
import classes.ImageFrom;
import classes.LiteralType;
import classes.LoginResult;
import classes.MutateResultStatus;
import classes.ReportOutAttribute;
import classes.ReportOutCell;
import classes.ReportOutColumn;
import classes.ReportOutOption;
import classes.ReportOutRow;
import classes.ReportOutput;
import classes.TopDeclType;
import classes.TrackSizeType;
import classes.TypeKind;
import classes.VerificationDataByToken;
import d3e.core.DFile;
import d3e.core.RPCConstants;
import d3e.core.SchemaConstants;
import gqltosql.schema.DClazz;
import gqltosql.schema.DModel;
import gqltosql.schema.DModelType;
import gqltosql.schema.FieldPrimitiveType;
import models.AnonymousUser;
import models.Avatar;
import models.BaseUser;
import models.BaseUserSession;
import models.ChangePasswordRequest;
import models.D3EImage;
import models.EmailMessage;
import models.OneTimePassword;
import models.PushNotification;
import models.ReportConfig;
import models.ReportConfigOption;
import models.SMSMessage;
import models.VerificationData;
import models.VerificationDataByTokenRequest;

@org.springframework.stereotype.Service
public class ModelSchema extends AbstractModelSchema {
  protected void createAllEnums() {
    addEnum(ConnectionStatus.class, SchemaConstants.ConnectionStatus);
    addEnum(MutateResultStatus.class, SchemaConstants.MutateResultStatus);
    addEnum(ChangeEventType.class, SchemaConstants.ChangeEventType);
    addEnum(ColumnWidthType.class, SchemaConstants.ColumnWidthType);
    addEnum(TrackSizeType.class, SchemaConstants.TrackSizeType);
    addEnum(IconType.class, SchemaConstants.IconType);
    addEnum(ImageFrom.class, SchemaConstants.ImageFrom);
    addEnum(DBResultStatus.class, SchemaConstants.DBResultStatus);
    addEnum(ASyncType.class, SchemaConstants.ASyncType);
    addEnum(ArrayType.class, SchemaConstants.ArrayType);
    addEnum(CommentType.class, SchemaConstants.CommentType);
    addEnum(LiteralType.class, SchemaConstants.LiteralType);
    addEnum(TopDeclType.class, SchemaConstants.TopDeclType);
    addEnum(TypeKind.class, SchemaConstants.TypeKind);
  }

  protected void createAllTables() {
    addTable(
        new DModel<DFile>(
            "DFile", SchemaConstants.DFile, 4, 0, "_dfile", DModelType.MODEL, () -> new DFile()));
    addTable(
        new DModel<AnonymousUser>(
                "AnonymousUser",
                SchemaConstants.AnonymousUser,
                0,
                2,
                "_anonymous_user",
                DModelType.MODEL,
                () -> new AnonymousUser())
            .creatable());
    addTable(
        new DModel<Avatar>(
            "Avatar",
            SchemaConstants.Avatar,
            2,
            0,
            "_avatar",
            DModelType.MODEL,
            () -> new Avatar()));
    addTable(
        new DModel<BaseUser>(
                "BaseUser", SchemaConstants.BaseUser, 2, 0, "_base_user", DModelType.MODEL)
            .creatable());
    addTable(
        new DModel<BaseUserSession>(
                "BaseUserSession",
                SchemaConstants.BaseUserSession,
                1,
                0,
                "_base_user_session",
                DModelType.MODEL)
            .creatable());
    addTable(
        new DModel<ChangePasswordRequest>(
                "ChangePasswordRequest",
                SchemaConstants.ChangePasswordRequest,
                1,
                0,
                "_change_password_request",
                DModelType.MODEL,
                () -> new ChangePasswordRequest())
            .trans()
            .creatable());
    addTable(
        new DModel<D3EImage>(
                "D3EImage",
                SchemaConstants.D3EImage,
                4,
                0,
                "_d3eimage",
                DModelType.MODEL,
                () -> new D3EImage())
            .emb());
    addTable(
        new DModel<EmailMessage>(
                "EmailMessage",
                SchemaConstants.EmailMessage,
                10,
                0,
                "_email_message",
                DModelType.MODEL,
                () -> new EmailMessage())
            .trans()
            .creatable());
    addTable(
        new DModel<OneTimePassword>(
                "OneTimePassword",
                SchemaConstants.OneTimePassword,
                9,
                0,
                "_one_time_password",
                DModelType.MODEL,
                () -> new OneTimePassword())
            .creatable());
    addTable(
        new DModel<PushNotification>(
                "PushNotification",
                SchemaConstants.PushNotification,
                4,
                0,
                "_push_notification",
                DModelType.MODEL,
                () -> new PushNotification())
            .trans()
            .creatable());
    addTable(
        new DModel<ReportConfig>(
            "ReportConfig",
            SchemaConstants.ReportConfig,
            2,
            0,
            "_report_config",
            DModelType.MODEL,
            () -> new ReportConfig()));
    addTable(
        new DModel<ReportConfigOption>(
            "ReportConfigOption",
            SchemaConstants.ReportConfigOption,
            2,
            0,
            "_report_config_option",
            DModelType.MODEL,
            () -> new ReportConfigOption()));
    addTable(
        new DModel<SMSMessage>(
                "SMSMessage",
                SchemaConstants.SMSMessage,
                5,
                0,
                "_smsmessage",
                DModelType.MODEL,
                () -> new SMSMessage())
            .trans()
            .creatable());
    addTable(
        new DModel<VerificationData>(
                "VerificationData",
                SchemaConstants.VerificationData,
                6,
                0,
                "_verification_data",
                DModelType.MODEL,
                () -> new VerificationData())
            .creatable());
    addTable(
        new DModel<VerificationDataByTokenRequest>(
                "VerificationDataByTokenRequest",
                SchemaConstants.VerificationDataByTokenRequest,
                1,
                0,
                "null",
                DModelType.MODEL,
                () -> new VerificationDataByTokenRequest())
            .trans()
            .creatable());
    addTable(
        new DModel<ReportOutput>(
            "ReportOutput",
            SchemaConstants.ReportOutput,
            5,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutput()));
    addTable(
        new DModel<ReportOutOption>(
            "ReportOutOption",
            SchemaConstants.ReportOutOption,
            2,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutOption()));
    addTable(
        new DModel<ReportOutColumn>(
            "ReportOutColumn",
            SchemaConstants.ReportOutColumn,
            3,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutColumn()));
    addTable(
        new DModel<ReportOutAttribute>(
            "ReportOutAttribute",
            SchemaConstants.ReportOutAttribute,
            2,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutAttribute()));
    addTable(
        new DModel<ReportOutRow>(
            "ReportOutRow",
            SchemaConstants.ReportOutRow,
            4,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutRow()));
    addTable(
        new DModel<ReportOutCell>(
            "ReportOutCell",
            SchemaConstants.ReportOutCell,
            4,
            0,
            null,
            DModelType.STRUCT,
            () -> new ReportOutCell()));
    addTable(
        new DModel<DBResult>(
            "DBResult",
            SchemaConstants.DBResult,
            2,
            0,
            null,
            DModelType.STRUCT,
            () -> new DBResult()));
    addTable(
        new DModel<LoginResult>(
            "LoginResult",
            SchemaConstants.LoginResult,
            4,
            0,
            null,
            DModelType.STRUCT,
            () -> new LoginResult()));
    addTable(
        new DModel<VerificationDataByToken>(
            "VerificationDataByToken",
            SchemaConstants.VerificationDataByToken,
            3,
            0,
            null,
            DModelType.STRUCT,
            () -> new VerificationDataByToken()));
    addDFileFields();
  }

  protected void addFields() {
    new ModelSchema1(allTypes).createAllTables();
    new StructSchema1(allTypes).createAllTables();
  }

  protected void recordAllChannels() {
    recordNumChannels(0);
  }

  protected void recordAllRPCs() {
    recordNumRPCs(2);
    DClazz UniqueChecker = addRPCClass("UniqueChecker", RPCConstants.UniqueChecker, 1);
    populateRPC(
        UniqueChecker,
        RPCConstants.UniqueCheckerCheckTokenUniqueInOneTimePassword,
        "checkTokenUniqueInOneTimePassword",
        SchemaConstants.Boolean,
        new gqltosql.schema.DParam(SchemaConstants.OneTimePassword),
        new gqltosql.schema.DParam(SchemaConstants.String));
    DClazz FileService = addRPCClass("FileService", RPCConstants.FileService, 1);
    populateRPC(
        FileService,
        RPCConstants.FileServiceCreateTempFile,
        "createTempFile",
        SchemaConstants.DFile,
        new gqltosql.schema.DParam(SchemaConstants.String),
        new gqltosql.schema.DParam(SchemaConstants.Boolean),
        new gqltosql.schema.DParam(SchemaConstants.String));
  }
}
