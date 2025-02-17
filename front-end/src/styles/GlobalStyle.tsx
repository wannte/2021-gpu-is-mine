import { createGlobalStyle } from "styled-components";
import fonts from "./fonts/fonts.styled";
import reset from "./reset";
import colors from "./colors";

const GlobalStyle = createGlobalStyle`
  ${reset}
  ${fonts}
  ${colors}

  :root {
    font-family: "Noto Sans KR";
    font-weight: 400;
  }

  html,
  body {
    height: 100%;
    width: 100%;
    margin: 0;
  }

  #root {
    height: 100%;
    position: relative;
  }
`;

export default GlobalStyle;
