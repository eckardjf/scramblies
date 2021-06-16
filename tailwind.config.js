const colors = require('tailwindcss/colors');

module.exports = {
  mode: 'jit',
  purge: {
    content: ["./resources/public/index.html", "./src/**/*.cljs"],
  },
  darkMode: "class", // or 'media' or 'class'
  theme: {
    extend: {
      colors: {
        violet: colors.violet,
      },
      transitionProperty: {
        'width': 'width',
      },
    },
  },
  variants: {},
  plugins: [require("@tailwindcss/forms")],
};