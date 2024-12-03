{ pkgs, lib, config, inputs, ... }:

{
  packages = [
  	pkgs.jq
	pkgs.jnv
  ];

  languages.scala = {
	enable = true;
	package = pkgs.scala_3;
	sbt.enable = true;
  };

  enterShell = ''
  	echo "Entering..."
  	type javac && type sbt
  	java -version
  '';

  enterTest = ''
    sbt test
  '';
}
