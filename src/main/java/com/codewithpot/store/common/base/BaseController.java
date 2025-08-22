package com.codewithpot.store.common.base;

import com.codewithpot.store.common.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;

@ApiResponses(value = {
        @ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Not found",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "500",
                description = "System Error",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        )
})
@RequestMapping("shoply/v1/")
public class BaseController {
}
