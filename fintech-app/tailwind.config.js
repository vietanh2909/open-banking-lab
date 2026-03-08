/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx,ts,tsx}"],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        primary: "#f0a742",
        "primary-dark": "#c97c20",
        "background-light": "#f8f7f6",
        "background-dark": "#221a10",
      },
      fontFamily: {
        display: ["Manrope"],
      },
    },
  },
  plugins: [require("@tailwindcss/forms")],
};