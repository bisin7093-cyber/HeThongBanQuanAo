const API_BASE=import.meta.env.VITE_API_BASE_URL||''
const TOKEN_KEY='blue-thread-token',USER_KEY='blue-thread-user'
export const getToken=()=>localStorage.getItem(TOKEN_KEY)
export const getUser=()=>{try{return JSON.parse(localStorage.getItem(USER_KEY)||'null')}catch{return null}}
export function saveSession(token,user){localStorage.setItem(TOKEN_KEY,token);localStorage.setItem(USER_KEY,JSON.stringify(user))}
export function clearSession(){localStorage.removeItem(TOKEN_KEY);localStorage.removeItem(USER_KEY)}
export async function api(path,options={}){const headers=new Headers(options.headers||{});if(options.body&&!(options.body instanceof FormData))headers.set('Content-Type','application/json');const token=getToken();if(token)headers.set('Authorization',`Bearer ${token}`);const response=await fetch(`${API_BASE}/api${path}`,{...options,headers});if(response.status===204)return null;const payload=await response.json().catch(()=>null);if(!response.ok){if(response.status===401)clearSession();throw new Error(payload?.message||payload?.error||'Có lỗi xảy ra. Vui lòng thử lại.')}return payload}
export const money=(value)=>new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND',maximumFractionDigits:0}).format(Number(value||0))
