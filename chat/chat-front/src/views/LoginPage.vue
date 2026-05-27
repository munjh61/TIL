<template>
<v-container>
        <v-row justify="center">
            <v-col cols="12" sm="4" md="6">
                <v-card>
                    <v-card-title class="text-h5 text-center">로그인</v-card-title>

                    <v-card-text>
                        <v-form @submit.prevent="doLogin">
                            <v-text-field label="이메일" v-model="email" required type="email" />
                            <v-text-field label="비밀번호" v-model="password" required type="password" />
                            <v-btn type="submit" color="primary" block>로그인</v-btn>
                        </v-form>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
    </v-container>
</template>
<script setup>
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';
import { ref } from 'vue';
// import { useRouter } from 'vue-router';

// const router = useRouter()

const email = ref('')
const password = ref('')

const doLogin = async () => {
    const loginData = {
        email : email.value,
        password : password.value
    }
    const res = await axios.post(`${import.meta.env.VITE_BASE_URL}/member/doLogin`, loginData)
    const token = res.data.token
    const role = jwtDecode(token).role
    const userEmail = jwtDecode(token).sub
    localStorage.setItem("token", token)
    localStorage.setItem("role", role)
    localStorage.setItem("email", userEmail)
    // router.push('/')
    window.location.href="/"
}
</script>
<style scoped></style>