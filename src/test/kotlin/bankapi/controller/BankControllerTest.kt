package bankapi.controller

import bankapi.model.Bank
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.status
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.*

@SpringBootTest
@AutoConfigureMockMvc
internal class BankControllerTest @Autowired constructor (
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper
) {

    val baseUrl = "/api/banks"

    @Nested
    @DisplayName("GET /api/banks")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetBanks{
        @Test
        @DirtiesContext
        fun `should return all banks`() {
            // when/then
            mockMvc.get(baseUrl)
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$[0].account_number") { value("1234") }
                }
        }
    }

    @Nested
    @DisplayName("GET /api/banks/{accountNumber}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetBank{
        @Test
        @DirtiesContext
        fun `should return the bank with the given account number`() {
            // given
            val accountNumber = "1234"
            // when/then
            mockMvc.get("$baseUrl/$accountNumber")
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.trust") { value("3.14") }
                    jsonPath("$.default_transaction_fee") { value("17") }
                }
        }

        @Test
        @DirtiesContext
        fun `should return NOT FOUND if account number does not exist`(){
            // given
            val accountNumber = "does_not_exist"
            // when/then
            mockMvc.get("$baseUrl/$accountNumber")
                .andDo { print() }
                .andExpect {
                    status { notFound() }
                }
        }
    }

    @Nested
    @DisplayName("POST /api/banks")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class PostNewBank{
        @Test
        @DirtiesContext
        fun `should add the new bank`() {
            // given
            val newBank = Bank("abc123", 31.415, 2)
            // when
            val performPost = mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(newBank)
            }
            // then
            performPost
                .andDo { print() }
                .andExpect {
                    status { isCreated() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                        json(objectMapper.writeValueAsString(newBank))
                    }
                }
            mockMvc.get("$baseUrl/${newBank.accountNumber}")
                .andExpect {
                    content {
                        json(objectMapper.writeValueAsString(newBank))
                    }
                }
        }

        @Test
        @DirtiesContext
        fun `should return BAD REQUEST if bank with given account number already exists`() {
            // given
            val invalidBank = Bank("1234", 1.0, 1)
            // when
            val performPost = mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(invalidBank)
            }
            // then
            performPost
                .andDo { print() }
                .andExpect {
                    status { isBadRequest() }
                }
        }
    }

    @Nested
    @DisplayName("PATCH /api/banks/{accountNumber}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class PatchExistingBank {
        @Test
        @DirtiesContext
        fun `should update an existing bank`() {
            // given
            val updatedBank = Bank("1234", 1.0, 1)
            // when
            val performPatch = mockMvc.patch(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(updatedBank)
            }
            // then
            performPatch
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                        json(objectMapper.writeValueAsString(updatedBank))
                    }
                }
            mockMvc.get("$baseUrl/${updatedBank.accountNumber}")
                .andExpect {
                    content {
                        json(objectMapper.writeValueAsString(updatedBank))
                    }
                }
        }

        @Test
        @DirtiesContext
        fun `should return BAD REQUEST if no bank with given account number exists`() {
            // given
            val invalidBank = Bank("does_not_exist", 1.0, 1)
            // when
            val performPatch = mockMvc.patch(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(invalidBank)
            }
            // then
            performPatch
                .andDo { print() }
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    @Nested
    @DisplayName("DELETE /api/banks/{accountNumber}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DeleteExistingBank {
        @Test
        @DirtiesContext
        fun `should delete the existing bank with the given account number`() {
            // given
            val accountNumber = "1234"
            // when/then
            mockMvc.delete("$baseUrl/$accountNumber")
                .andDo { print() }
                .andExpect {
                    status { isNoContent() }
                }
            mockMvc.get("$baseUrl/$accountNumber")
                .andDo { print() }
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @DirtiesContext
        fun `should return NOT FOUND if no bank with the given account number exists`() {
            // given
            val invalidAccountNumber = "does_not_exist"
            // when/then
            mockMvc.delete("$baseUrl/$invalidAccountNumber")
                .andDo { print() }
                .andExpect {
                    status { isNotFound() }
                }
        }
    }
}