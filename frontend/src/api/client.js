import axios from "axios";

const client = axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: true,
});

client.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem("accessToken");
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

export default client;
