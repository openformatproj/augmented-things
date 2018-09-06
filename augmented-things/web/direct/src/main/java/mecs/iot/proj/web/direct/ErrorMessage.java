package mecs.iot.proj.web.direct;

public class ErrorMessage {
	public final String BAD_QUERY = "{\"error\": \"bad_query\"}";
	public final String NO_CMD = "{\"error\": \"no_commands\"}";
	public final String UNREC_CMD = "{\"error\": \"unrecognized_command\"}";
	public final String GET_ERR = "{\"error\": \"GET_parser_error\"}";
	public final String INVALID_QUERY = "{\"error\": \"invalid_query\"}";
	public final String UNREG = "{\"error\": \"server_unregistered\"}";
	public final String OM2M_FAIL = "{\"error\": \"om2m_failed\"}";
	public final String POST_ERR = "{\"error\": \"POST_error\"}";
	public final String DEL_ERR = "{\"error\": \"DEL_error\"}";
	public final String INTERR_WAIT = "{\"error\":\"interrupted_waiting_registration\"}";
	public final String REG_FAIL = "{\"error\":\"registration_failed\"}";
	public final String REG_WAIT = "{\"error\":\"still_registering_retry\"}";
}
