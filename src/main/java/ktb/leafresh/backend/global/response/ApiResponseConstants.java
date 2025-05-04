package ktb.leafresh.backend.global.response;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public class ApiResponseConstants {

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "201", description = "리소스 생성됨",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "204", description = "콘텐츠 없음")
    })
    public @interface SuccessResponses {}

    @ApiResponses({
            @ApiResponse(responseCode = "301", description = "영구 이동"),
            @ApiResponse(responseCode = "302", description = "임시 이동"),
            @ApiResponse(responseCode = "304", description = "변경 없음")
    })
    public @interface RedirectResponses {}

    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "409", description = "충돌 발생",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "422", description = "처리할 수 없는 요청",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public @interface ClientErrorResponses {}

    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "503", description = "서비스 이용 불가",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "504", description = "Gateway Timeout",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public @interface ServerErrorResponses {}
}
