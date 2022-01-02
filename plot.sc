__config()->
{
    'commands' ->
    {
        '' -> _() -> print(player(), ''),
        'claim <name>' -> _(name) -> plot_claim(name, 'lime', false),
        'claim <name> <color>' -> _(name, color) -> plot_claim(name, color, false),
        'claim <name> <color> overwrite' -> _(name, color) -> plot_claim(name, color, true),
        'unclaim' -> _() -> plot_unclaim(false),
        'unclaim confirm' -> _() -> plot_unclaim(true),
        'query' -> _() -> plot_query(player()~'pos'),
        'join <name>' -> 'plot_join',
        'delete <name>' -> 'plot_delete',
        'list' -> 'plot_list'
    },
    'arguments' ->
    {
        'name' -> {'type' -> 'string'},
        'color' -> {'type' -> 'string', 'options' -> ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black', 'none']}
    },
    'scope' -> 'global'
};

plot_claim(name, color, overwrite) ->
(
    player = player();
    pos = player~'pos' / 512;
    pos = [ceil(pos:0) - 1, ceil(pos:2) - 1];

    if(list_files('', 'json')~'plots' == null,
        write_file('plots', 'json', {'plots' -> [{'owners' -> [player], 'positions' -> [pos], 'name' -> name, 'subplots' -> []}]});
        print(player, format('r Region ', 't ' + pos, 'r  claimed for new plot ', 't \"' + name + '\" ', 'r by ', 't ' + player));
        highlight_region(pos, color);
        return();
    );

    plots = read_file('plots', 'json');

    i = 0;
    j = 0;
    for(plots:'plots', plot = _; j += 1; for(plot:'positions',
        if(_ == pos,
            if(plot:'name' == name,
                print(player, format('l This region is already part of ', 't \"' + name + '\"'));
                return()
            );
            if(overwrite,
                if(length(plot:'positions') > 1, delete(plot:'positions', i),
                    print(player, format('l Overwriting this region will delete plot ', 't \"' + plot:'name' + '\"', 'l . If you want to continue, run ', 'm [/plot delete ' + plot:'name' + ']', '!/plot delete ' + plot:'name', 'l  and then you can claim this region'));
                    return()
                );
                if(!add_region_to_plot(plots, pos, name, color, player),
                    new_plot_entry(plots, pos, name, player, color, player)
                )
            ,    
                if(length(plot:'positions') == 1,
                    print(player, format('l Overwriting this region will delete plot ', 't \"' + plot:'name' + '\"', 'l . If you want to continue, run ', 'm [/plot delete ' + plot:'name' + ']', '!/plot delete ' + plot:'name', 'l  and then you can claim this region')),
                print(player, format('l Region already taken by plot ', 't \"' + plot:'name' + '\"', 'l . Run ', 'm [/plot claim ' + name + ' <color> overwrite]', '!/plot claim ' + name + ' ' + color + ' overwrite', 'l  to overwrite it'))
                )
            );
            return()
        );
        i += 1;
    ));

    if(!add_region_to_plot(plots, pos, name, color, player),
        new_plot_entry(plots, pos, name, player, color, player)
    )
);

add_region_to_plot(plots, pos, name, color, player) ->
(
    for(plots:'plots', if(_:'name' == name,
        _:'positions':length(_:'positions') = pos;
        write_file('plots', 'json', plots);
        print(player, format('r Region ', 't ' + pos, 'r  added to plot ', 't \"' + name + '\"'));
        highlight_region(pos, color);
        return(true)
    ));
    return(false)
);

new_plot_entry(plots, pos, name, owners, color, player) ->
(
    plots:'plots':length(plots:'plots') = {'owners' -> owners, 'positions' -> [pos], 'name' -> name, 'subplots' -> []}; 
    print(player, format('r Region ', 't ' + pos, 'r  claimed for new plot ', 't \"' + name + '\" ', 'r by ', 't ' + owners));
    highlight_region(pos, color);
    write_file('plots', 'json', plots);
);

plot_unclaim(confirm) ->
(
    plots = read_file('plots', 'json');
    player = player();
    pos = player~'pos' / 512;
    pos = [ceil(pos:0) - 1, ceil(pos:2) - 1];

    i = 0;
    j = 0;
    for(plots:'plots',
        plot = _;
        for(plot:'positions',
            if(_ == pos,
                if(length(plot:'positions') == 1,
                        print(player, format('l Unclaiming this region will delete plot ', 't \"' + plot:'name' + '\"', 'l . If you want to continue, run ', 'm [/plot delete ' + plot:'name' + ']', '!/plot delete ' + plot:'name'));
                        return()
                );
                if(confirm,
                    delete(plot:'positions', j);
                    write_file('plots', 'json', plots);
                    print(player, format('r Unclaiming region ', 't ' + pos, 'r  from ', 't ' + plot:'name'));
                    unhighlight_region(pos);
                    return()
                ,
                    print(player, format('r Current region is part of ', 't \"' + plot:'name' + '\"', 'r , owned by ', 't ' + plot:'owners', 'r , with ', 't ' + length(plot:'positions'),
                    'r  regions total. Click ', 'm HERE', '!/plot unclaim confirm', 'r  to confirm'));
                );
                return()
            );
            j += 1
        );
        i += 1
    );
    print(player, format('l Current region is unclaimed'))
);

plot_query(pos) ->
(
    plots = read_file('plots', 'json');
    pos = [ceil(pos:0 / 512) - 1, ceil(pos:2 / 512) - 1];
    player = player();

    for(plots:'plots', plot = _; for(plot:'positions', if(_ == pos,
        print(player, format('r Current region is part of ', 't \"' + plot:'name' + '\"', 'r , owned by ', 't ' + plot:'owners', 'r , with ', 't ' + length(plot:'positions'), 'r  regions total'));
        return()
    )))
);

plot_join(name) ->
(
    plots = read_file('plots', 'json');
    player = player();

    for(plots:'plots', if(_:'name' == name,
        for(_:'owners', if(_ == player,
            print(player, format('t ' + player, 'l  already owns plot ', 't \"' + name + '\"'));
            return();
        ));
        _:'owners':length(_:'owners') = player();
        write_file('plots', 'json', plots);
        print(player, format('t ' + player, 'r  added to plot ', 't \"' + name + '\"'));
    ));
    print(player, format('l plot ', 't \"' + name + '\" ', 'l does not exist'))
);

plot_delete(name) ->
(
    plots = read_file('plots', 'json');
    player = player();

    i = 0;
    for(plots:'plots',
        if(_:'name' == name,
            delete(plots:'plots', i);
            write_file('plots', 'json', plots);
            print(player, format('r ' + 'plot ', 't \"' + name + '\" ', 'r deleted'));
            for(_:'positions', unhighlight_region(_));
            return();
        );
        i += 1
    );
    print(player, format('l plot ', 't \"' + name + '\" ', 'l does not exist'))
);

plot_list() ->
(
    plots = read_file('plots', 'json');
    player = player();

    l = length(plots:'plots');
    if(l == 0,
        print(player, format('l No plots configured'));
        return()
    );
    for(plots:'plots',
        print(player, format('r - ', 't ' + _:'name', 'r  owned by ', 't ' + _:'owners', 'r  with regions ', 't ' + _:'positions'))
    )
);

vanilla_fill(pos1, pos2, block) ->
(
    run('fill ' + pos1:0 + ' ' + pos1:1 + ' ' + pos1:2 + ' ' + pos2:0 + ' ' + pos2:1 + ' ' + pos2:2 + ' ' + block)
);

setblock(pos, block) ->
(
    run('setblock ' + pos:0 + ' ' + pos:1 + ' ' + pos:2 + ' ' + block)
);

highlight_region(pos, color) ->
(
    if(color == 'none', return());
    pos = pos * 512;
    pos = [pos:0, 0, pos:1];
    block = color + '_stained_glass';

    add_chunk_ticket(pos + [256, 0, 256], 'teleport', 16);
    schedule(1, _(outer(pos), outer(block)) ->
    (
        vanilla_fill(pos, pos + [511, 0, 0], block);
        vanilla_fill(pos + [511, 0, 0], pos + [511, 0, 511], block);
        vanilla_fill(pos + [511, 0, 511], pos + [0, 0, 511], block);
        vanilla_fill(pos + [0, 0, 511], pos, block);
    ))
);

unhighlight_region(pos) ->
(
    if(pos:0 < 0 && pos:1 < 0, highlight_region(pos, 'white'); return());
    if((pos:0 >= 0 && pos:1 < 0) || (pos:0 >= 0 && pos:1 >= 0), highlight_region(pos, 'blue'); return());

    pos = pos * 512;
    pos = [pos:0, 0, pos:1];
    add_chunk_ticket(pos + [256, 0, 256], 'teleport', 16);
    schedule(1, _(outer(pos), outer(block)) ->
        c_for(i = 0, i <= 496, i += 16,
            setblock(pos + [i, 0, 0], 'blue_stained_glass');
            vanilla_fill(pos + [i + 1, 0, 0], pos + [i + 14, 0, 0], 'white_stained_glass');
            setblock(pos + [i + 15, 0, 0], 'blue_stained_glass');

            setblock(pos + [0, 0, i], 'blue_stained_glass');
            vanilla_fill(pos + [0, 0, i + 1], pos + [0, 0, i + 14], 'white_stained_glass');
            setblock(pos + [0, 0, i + 15], 'blue_stained_glass');

            setblock(pos + [i, 0, 511], 'blue_stained_glass');
            vanilla_fill(pos + [i + 1, 0, 511], pos + [i + 14, 0, 511], 'white_stained_glass');
            setblock(pos + [i + 15, 0, 511], 'blue_stained_glass');

            setblock(pos + [511, 0, i], 'blue_stained_glass');
            vanilla_fill(pos + [511, 0, i + 1], pos + [511, 0, i + 14], 'white_stained_glass');
            setblock(pos + [511, 0, i + 15], 'blue_stained_glass');
        )
    )
)