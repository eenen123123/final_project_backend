/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    // ⭐️ 중요: templates 내부의 모든 HTML 파일(레이아웃, 본문 포함)을 감시 대상으로 지정
    "./src/main/resources/templates/**/*.html",
    "./src/main/resources/templates/*.html",
    "./src/main/resources/static/js/**/*.js",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
};
