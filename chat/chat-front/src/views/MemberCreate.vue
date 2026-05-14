<template>
    <v-container>
        <v-row justify="center">
            <v-col cols="12" sm="4" md="6">
                <v-card>
                    <v-card-title class="text-h5 text-center">회원가입</v-card-title>

                    <v-card-text>
                        <v-form @submit.prevent="memberCreate">
                            <v-text-field label="이름" v-model="name" required />
                            <v-text-field label="이메일" v-model="email" required type="email" />
                            <v-text-field label="비밀번호" v-model="password" required type="password" />
                            <v-btn type="submit" color="primary" block>등록</v-btn>
                        </v-form>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
    </v-container>
</template>
<script setup>
import axios from 'axios';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter()

const name = ref('')
const email = ref('')
const password = ref('')

const memberCreate = async () => {
    const data = {
        name : name.value,
        email : email.value,
        password : password.value
    }
    await axios.post(`${import.meta.env.VITE_BASE_URL}/member/create`, data)
    router.push("/")
}
</script>
<style scoped></style>