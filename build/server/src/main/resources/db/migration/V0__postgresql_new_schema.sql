create sequence _d3e_sequence;
create table _dfile ( _id varchar(255) not null, _name varchar(255), _size int8 not null, _mime_type varchar(255), primary key (_id) );

create table _anonymous_user(_id int8 not null, _save_status int4, primary key (_id));

create table _avatar(_id int8 not null, _save_status int4, _image_size int8 not null, _image_width int8 not null, _image_height int8 not null, _image_file_id varchar(255), _create_from varchar(255), primary key (_id));

create table _base_user(_id int8 not null, _save_status int4, _is_active bool, _device_token text, primary key (_id));

create table _base_user_session(_id int8 not null, _save_status int4, _user_session_id text not null, primary key (_id));

create table _one_time_password(_id int8 not null, _save_status int4, _input varchar(255) not null, _input_type varchar(255) not null, _user_type varchar(255) not null, _success bool not null, _error_msg varchar(255), _token varchar(255), _code varchar(255), _user_id int8, _expiry timestamp, primary key (_id));

create table _report_config(_id int8 not null, _save_status int4, _identity varchar(255) not null, primary key (_id));

create table _report_config_values_a912b7(_report_config_id int8 not null, _values_id int8 not null, _values_order int4 not null, primary key (_report_config_id, _values_order));

create table _report_config_option(_id int8 not null, _save_status int4, _identity varchar(255) not null, _value varchar(255) not null, primary key (_id));

create table _verification_data(_id int8 not null, _save_status int4, _method varchar(255) not null, _context varchar(255) not null, _token varchar(255), _subject varchar(255), _body varchar(255) not null, _processed bool, primary key (_id));

alter table if exists _report_config_values_a912b7 drop constraint if exists UK_f63b8ce4f8377eeedcf87d62bd06f43c;
alter table if exists _report_config_values_a912b7 add constraint UK_f63b8ce4f8377eeedcf87d62bd06f43c unique (_values_id) DEFERRABLE INITIALLY DEFERRED;

alter table if exists _anonymous_user add constraint FKeade347c9b950d74e0769e3329c0848a foreign key (_id) references _base_user DEFERRABLE INITIALLY DEFERRED;

alter table if exists _one_time_password add constraint FKd85dc405a5145f1d14e1f920c7ad1330 foreign key (_user_id) references _base_user DEFERRABLE INITIALLY DEFERRED;

alter table if exists _report_config_values_a912b7 add constraint FKf63b8ce4f8377eeedcf87d62bd06f43c foreign key (_values_id) references _report_config_option DEFERRABLE INITIALLY DEFERRED;
alter table if exists _report_config_values_a912b7 add constraint FKc75f7ac5ec68527db571109ace37d022 foreign key (_report_config_id) references _report_config DEFERRABLE INITIALLY DEFERRED;
