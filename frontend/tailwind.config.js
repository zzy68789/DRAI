/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        blue: {
          50: '#edfdfa',
          100: '#ccfbf1',
          200: '#99f6e4',
          300: '#5eead4',
          400: '#2dd4bf',
          500: '#14b8a6',
          600: '#0d9488',
          700: '#0f766e',
          800: '#115e59',
          900: '#134e4a',
          950: '#042f2e',
        },
      },
      fontFamily: {
        // 现代无衬线字体栈：优先系统默认，保证清晰
        sans: [
          'Inter', 
          'system-ui', 
          '-apple-system', 
          'BlinkMacSystemFont', 
          '"Segoe UI"', 
          'Roboto', 
          '"Helvetica Neue"', 
          'Arial', 
          '"Noto Sans"', 
          '"PingFang SC"',    // Mac 中文
          '"Hiragino Sans GB"', // Mac 中文
          '"Microsoft YaHei"',  // Win 中文
          '"WenQuanYi Micro Hei"', 
          'sans-serif'
        ],
        // 产品标题与关键数字使用更紧实的工程化字体栈
        display: ['"IBM Plex Sans"', 'Inter', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'Menlo', 'Consolas', '"Courier New"', 'monospace'],
      },
      typography: {
        DEFAULT: {
          css: {
            maxWidth: 'none',
            color: '#374151', // Gray-700，比纯黑柔和
            // 基础行高设置
            p: {
              marginTop: '1.2em',
              marginBottom: '1.2em',
              lineHeight: '1.75', 
              letterSpacing: '0.01em', // 稍微加一点字间距提升呼吸感
            },
            // 标题设置
            h1: {
              color: '#111827', // Gray-900
              fontWeight: '800',
              marginTop: '0',
              marginBottom: '0.8em',
              lineHeight: '1.2',
            },
            h2: {
              color: '#1f2937', // Gray-800
              fontWeight: '700',
              marginTop: '2em',
              marginBottom: '1em',
              lineHeight: '1.3',
            },
            h3: {
              color: '#1f2937',
              fontWeight: '600',
              marginTop: '1.5em',
              marginBottom: '0.6em',
            },
            // 列表
            li: {
              marginTop: '0.5em',
              marginBottom: '0.5em',
            },
          },
        },
      },
    },
  },
  plugins: [
    require('@tailwindcss/typography'),
  ],
}
