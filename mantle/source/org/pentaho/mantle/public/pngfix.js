/*
 
Correctly handle PNG transparency in Win IE 5.5 & 6.
http://homepage.ntlworld.com/bobosola. Updated 18-Jan-2006.

Use in <HEAD> with DEFER keyword wrapped in conditional comments:
<!--[if lt IE 7]>
<script defer type="text/javascript" src="pngfix.js"></script>
<![endif]-->

add  fixPNGs() call to your document's body onload attribute
<body onLoad="fixPNGs()">



WARNINGS!!
Problem: Containers with PNGs as backgrounds that "stretch" (repeat-x,y) that also have a padding style will not scale properly. 
The filter will stretch the image into the padding area in error
Solution: Try to pad the parent or use margin instead.


Problem: Containers with PNGs that have a background-position other than "top left" or "left top" will not be converted to the filter 
method as we cannot position the filter image properly.
Solution: Do not enter a position of "top left" as this is the defualt. There is no workaround for other positionings.

Problem: Filter honors CSS heights moreso than HTML. If an element is defined as 10px, but in that element something makes it 
15px (pushing the parent), the filter will still render it as 10px. 
Solution: This is more a problem of a style not matching implementation. 

*/

function fixPNGs(){
    var arVersion = navigator.appVersion.split("MSIE")
    var version = parseFloat(arVersion[1])

    if ((version >= 5.5) && (document.body.filters)) 
    {
    
       if(!window.baseURL){
            window.baseURL = "";
       } 
       
      //replaces images with spans
       for(var i=0; i<document.images.length; i++)
       {
          var img = document.images[i]
          var imgName = img.src.toUpperCase()
          if (imgName.substring(imgName.length-3, imgName.length) == "PNG")
          {
            
             img.style.height = img.height + "px";
             img.style.width = img.width+"px";
             img.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader"
             + "(src=\'" + img.src + "\', sizingMethod='scale');\""; 
             img.src = "/pentaho-style/images/spacer.gif"
             img.style.position = "relative";
             
          }
       }
       
       //search for all containers (div,span,td, etc) with a PNG backgroundImage
       var list = searchForBGs([], document.body);
       for(var i=0; i<list.length;  i++){
           fixStyle(list[i].style);
       }

       //Search defined classes in all stylesheets for PNG background images
        for(var i=0; i<document.styleSheets.length; i++){
            var sheet = document.styleSheets[i].rules;
            for(var y=0; y<sheet.length; y++){
                var background = sheet[y].style.backgroundImage;
                if(!background){
                    continue;
                }
                if(background.toLowerCase().indexOf(".png") > -1){
                    fixStyle(sheet[y].style);
                }
            }
        }

    }

}

/*==================================================================
Recurse to find all elements that have a style with a PNG backgroundImage
====================================================================*/
function searchForBGs(list, node){
  if(node.style && node.style.backgroundImage && node.style.backgroundImage.toLowerCase().indexOf(".png") > -1){
    list[list.length] = node;
  }
  for(var i=0; i<node.childNodes.length; i++){
    list = searchForBGs(list, node.childNodes[i]);
  }
  return list;
}

/*==================================================================
For the given style, replace the backgroundImage with the AlphaImageLoader Filter.
====================================================================*/
function fixStyle(style){
    if(style.backgroundPosition != "" && style.backgroundPosition.toLowerCase() != "top left" && style.backgroundPosition.toLowerCase() != "left top"){
        //It's not possible
        return;
    }
    var background = style.backgroundImage;
   if(background.indexOf("url(") > -1){
        background = background.substring(background.indexOf("url(")+4, background.indexOf(")"));
   }
   background.replace("'","");
   background.replace("\"","");
   var repeat = style.backgroundRepeat;
   var sizingMethod = (repeat && repeat.toLowerCase() != "no-repeat")? "scale":"image";
   style.backgroundImage = "";
   //style.position = "relative";
   style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\'" + window.baseURL + background + "\', sizingMethod='"+sizingMethod+"')";
}